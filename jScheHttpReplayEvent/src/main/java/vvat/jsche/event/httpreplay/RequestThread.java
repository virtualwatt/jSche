package vvat.jsche.event.httpreplay;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

public class RequestThread implements Runnable {

	private static final Logger log = Logger.getLogger(RequestThread.class);

	private ByteArrayData httpRequest;

	private String host;

	private int port;

	private boolean sslOn;

	private CountDownLatch latch;

	public RequestThread(ByteArrayData httpRequest, String host, int port,  boolean sslOn, CountDownLatch latch) {
		this.httpRequest = httpRequest;
		this.host = host;
		this.port = port;
		this.sslOn = sslOn;
		this.latch = latch;
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		try {
			latch.await();
			HttpReplay.send(httpRequest, host, port, sslOn);
		} catch (Exception e) {
			log.error("Request sending failed to host=" + host + ", port=" + port, e);
		} finally {
			log.info("Request sending has taken [" + (System.currentTimeMillis() - startTime) + "] ms.");
		}
	}
}
