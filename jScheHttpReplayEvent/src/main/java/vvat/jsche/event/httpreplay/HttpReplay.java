package vvat.jsche.event.httpreplay;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocketFactory;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import vvat.jsche.core.scheduleconfig.JScheEvent;

/**
 *
 *
 */
@XmlRootElement
public class HttpReplay extends JScheEvent
{
	private static final Logger log = Logger.getLogger(HttpReplay.class);
	
	private String host;
	private int port;
	private String requestFile;
	private static String defaultDir;
	
	static {
		defaultDir = System.getProperty("httpReplayDir");
		if (defaultDir == null || defaultDir.isEmpty() || !new File(defaultDir).isDirectory())
			defaultDir = System.getProperty("configDir");
		defaultDir += '/';
	}
	
	/**
	 * For single execution instance
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException
    {
    	if (args.length == 0 || args.length > 4) {
    		System.out.println("Arguments: [-batch(<total requests>[,<requests in batch>[,<delay between batches>]])] <HTTP request file> [<host> <port>]");
    		return;
    	}
    	String arg0 = args[0];
    	int argInc = arg0.startsWith("-batch(") ? 1 : 0;
    	String requestFile = args[0 + argInc];
    	boolean result;
    	String host = null;
    	int port = 0;
    	if (args.length == 3 + argInc) {
    		host = args[1 + argInc];
    		port = Integer.parseInt(args[2 + argInc]);
    	}
    	if (argInc == 0)
    		result = performRequest(requestFile, host, port);
    	else {
    		String[] batchParams = arg0.substring("-batch(".length(), arg0.length() - 1).split(",");
    		int requestsCount = Integer.parseInt(batchParams[0]);
    		int batchSize = batchParams.length > 1 ? Integer.parseInt(batchParams[1]) : requestsCount;
    		long batchInterval = batchParams.length > 2 ? Long.parseLong(batchParams[2]) : 0;
    		result = performBatchRequests(requestFile, host, port, false, requestsCount, batchSize, batchInterval);
    	}
    	if (result)
    		log.info("Success.");
   		else
       		log.error("Failure!");
    }

	private static boolean performRequest(String requestFile) throws IOException {
		return performRequest(requestFile, null, 0);
	}

	private static final Pattern hostPtrn =
			Pattern.compile("^.*?Host:\\s*(?<host>[^\r\n:]+)(?::(?<port>[^\r\n]+))?(?:\r?\n.*?)?\r?\n\r?\n", Pattern.DOTALL);

	private static boolean performRequest(String requestFile, String host, int port) throws IOException {
    	ByteArrayData httpRequest = new FileSource(requestFile).getFileData();
		if (host == null || host.isEmpty()) {
			String dataString = httpRequest.toString();
			Matcher matcher = hostPtrn.matcher(dataString);
			if (matcher.find()) {
				host = matcher.group("host").trim();
				String portGrp = matcher.group("port");
				if (portGrp != null)
					port = Integer.parseInt(portGrp.trim());
				else
					port = 80;
			} else {
				log.error("No 'Host' header found, specify host & port explicetly: " + requestFile);
				return false;
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Performing request [").append(requestFile).append("] to ").append(host).append(':').append(port);
		log.info(sb.toString());
		return send(httpRequest, host, port);
	}

	public static boolean send(ByteArrayData httpRequest, String hostname, int port) throws IOException {
		return send(httpRequest, hostname, port, false);
	}

	public static boolean send(ByteArrayData request, String hostname, int port, boolean sslOn) throws IOException {
		OutputStream outStream = null;
		InputStream inStream = null;
		Socket socket = null;
		try {
			if (sslOn) {
				SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
				socket = ssf.createSocket(hostname, port);
			} else {
				socket = new Socket(hostname, port);
			}
			outStream = socket.getOutputStream();
			outStream.write(request.getContent());
			inStream = socket.getInputStream();
			return readResponse(inStream);
		} finally {
			if(socket != null){
				socket.close();
			}
			if(outStream != null){
				outStream.close();
			}
			if(inStream != null){
				inStream.close();
			}
		}
	}
	
	private static enum BodyTransMethod {
		UNKNOWN, CONTENT_LENGTH, CHUNKED
	}
	private static final Pattern patTransferEncoding = Pattern.compile("^Transfer-Encoding:\\s*(\\S+?)\\s*$");
	private static final Pattern patContentLength = Pattern.compile("^Content-Length:\\s*(\\S+?)\\s*$");
	private static final int defaultChunkedBufLen = 65536;

	private static boolean readResponse(InputStream inStream) {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inStream));
		try {
			String firstLine = bufferedReader.readLine();
			log.info("Response: " + firstLine);
			if (firstLine == null)
				return false;
			if (System.getProperty("detailedHttpResponse") != null) {
				String line = null;
				StringBuilder sb = new StringBuilder();
				try {
					BodyTransMethod bodyTransMethod = BodyTransMethod.UNKNOWN;
					int len = 0;
					sb.append("Response details:\n=== Response headers ===\n");
					while (!(line = bufferedReader.readLine()).isEmpty()) {
						if (bodyTransMethod == BodyTransMethod.UNKNOWN) {
							Matcher m;
							if ((m = patContentLength.matcher(line)).find()) {
								bodyTransMethod = BodyTransMethod.CONTENT_LENGTH;
								len = Integer.parseInt(m.group(1));
							} else if ((m = patTransferEncoding.matcher(line)).find()) {
								if ("chunked".equals(m.group(1)))
									bodyTransMethod = BodyTransMethod.CHUNKED;
								else
									log.error("Unrecognized " + line);
							}
						}
						sb.append(line).append('\n');
					}
					char[] cbuf;
					sb.append("=== Response body ===\n");
					if (bodyTransMethod == BodyTransMethod.CONTENT_LENGTH) {
						cbuf = new char[len];
						bufferedReader.read(cbuf, 0, len);
						sb.append(cbuf);
					} else if (bodyTransMethod == BodyTransMethod.CHUNKED) {
						cbuf = new char[defaultChunkedBufLen];
						while ((len = Integer.parseInt((line = bufferedReader.readLine()), 16)) > 0) {
							if (len > cbuf.length)
								cbuf = new char[len];
							int read = 0;
							while (len > read) {
								int left = len - read;
								read += bufferedReader.read(cbuf, read, left);
							}
							line = bufferedReader.readLine(); // chunked block CRLF postfix
							sb.append(cbuf, 0, len);
						}
					} else {
						sb.append("=== WARN: Unknown body length, reading until the end of the stream...\n");
						while ((line = bufferedReader.readLine()) != null)
							sb.append(line).append('\n');
					}
					log.info(sb);
				} catch (Exception e) {
					log.error("Exception occurred collecting the full HTTP response, last buffer and line follows:");
					log.error(sb);
					log.error(line);
					throw e;
				}
			}
			int retCodeGrp = (int)firstLine.charAt(firstLine.indexOf(' ') + 1) - (int)'0';
			return retCodeGrp == 2 || retCodeGrp == 3;
		} catch (Exception e) {
			log.error("Error reading response", e);
			return false;
		}
	}

	@Override
	public boolean assertValid() {
		if (requestFile == null || requestFile.isEmpty()) {
			log.error("Request file isn't passed");
			return false;
		}
		if (!new File(requestFile).canRead()) {
			String requestFileTest = defaultDir + requestFile;
			if (!new File(requestFileTest).canRead()) {
				log.error("Can't read request file " + requestFile);
				return false;
			}
			else
				requestFile = requestFileTest;
		}
		return true;
	}

	@Override
	public void execute() {
		try {
			if (host != null)
				performRequest(requestFile, host, port);
			else
				performRequest(requestFile);
		} catch (IOException e) {
			log.error("Error sending the request", e);
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getRequestFile() {
		return requestFile;
	}

	public void setRequestFile(String requestFile) {
		this.requestFile = requestFile;
	}
	
	@Override
	public String toString() {
		return requestFile + " HTTP request executor";
	}

	public static boolean performBatchRequests(String requestFile, String host, int port, boolean sslOn, int requestsCount, int batchSize, long batchInterval) throws IOException, InterruptedException {
		ByteArrayData httpRequest = new FileSource(requestFile).getFileData();
		boolean result = true;
		long startTime = System.currentTimeMillis();
		while (requestsCount > 0) {
			log.info("____Request batch has started.");
			long batchStartTime = System.currentTimeMillis();
			CountDownLatch latch = new CountDownLatch(1);
			if (requestsCount < batchSize) {
				batchSize = requestsCount;
			}
			ExecutorService executor = Executors.newFixedThreadPool(batchSize);
			List<RequestThread> batch = new ArrayList<>(batchSize);
			for (int i = 0; i < batchSize; i++) {
				RequestThread requestThread = new RequestThread(httpRequest, host, port, sslOn, latch);
				executor.submit(requestThread);
				batch.add(requestThread);
			}
			executor.shutdown();
			latch.countDown();
			executor.awaitTermination(10, TimeUnit.MINUTES);
			requestsCount -= batchSize;
			log.info("____ Requests batch has taken [" + (System.currentTimeMillis() - batchStartTime) + "] ms.");
			Thread.sleep(batchInterval);
			for (RequestThread requestThread: batch) {
				if (!requestThread.getResult())
					result = false;
			}
		}
		log.info("**** Requests batches has taken [" + (System.currentTimeMillis() - startTime) + "] ms.");
		return result;
	}
}
