package vvat.jsche.event.httpreplay;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
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
	 */
	public static void main(String[] args) throws IOException
    {
    	if (args.length == 0 || args.length == 2 || args.length > 3) {
    		System.out.println("Arguments: -service | (<HTTP request file> [<host> <port>])");
    		return;
    	}
    	String requestFile = args[0];
    	boolean result;
    	if (args.length == 3) {
    		String host = args[1];
    		int port = Integer.parseInt(args[2]);
    		result = performRequest(requestFile, host, port);
    	} else
    		result = performRequest(requestFile);
    	System.out.println(result ? "Success." : "Failure!");
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

	private static boolean readResponse(InputStream inStream) {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inStream));
		try {
			String firstLine = bufferedReader.readLine();
			log.info("Response: " + firstLine);
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

	public static void performBatchRequests(String requestFile, String host, int port, boolean sslOn, int requestsCount, int batchSize, long batchInterval) throws IOException, InterruptedException {
		ByteArrayData httpRequest = new FileSource(requestFile).getFileData();
		long startTime = System.currentTimeMillis();
		while (requestsCount > 0) {
			log.info("____Request batch has started.");
			long batchStartTime = System.currentTimeMillis();
			CountDownLatch latch = new CountDownLatch(1);
			if (requestsCount < batchSize) {
				batchSize = requestsCount;
			}
			ExecutorService executor = Executors.newFixedThreadPool(batchSize);
			for (int i = 0; i < batchSize; i++) {
				executor.submit(new RequestThread(httpRequest, host, port, sslOn, latch));
			}
			executor.shutdown();
			latch.countDown();
			executor.awaitTermination(10, TimeUnit.MINUTES);
			requestsCount -= batchSize;
			log.info("____Request batch has taken [" + (System.currentTimeMillis() - batchStartTime) + "] ms.");
			Thread.sleep(batchInterval);
		}
		log.info("****Request batch has taken [" + (System.currentTimeMillis() - startTime) + "] ms.");
	}
}
