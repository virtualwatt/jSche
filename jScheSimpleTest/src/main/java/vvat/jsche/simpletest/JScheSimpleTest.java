package vvat.jsche.simpletest;

import org.apache.log4j.Logger;

import vvat.jsche.core.EventsClassesHolder;
import vvat.jsche.core.JSche;
import vvat.jsche.event.testevent.JScheTestEvent;

/**
 * @author Vitaliy Tkachenko
 */
public class JScheSimpleTest {

	private static final Logger log = Logger.getLogger(JScheSimpleTest.class);

	private static JSche jSche;

	public static void main(String[] args) {

		setEventsClasses();
		System.setProperty("configDir", ".");

		Runtime.getRuntime().addShutdownHook(new Thread("breaker") {
			public void run() {
				jSche.stop();
				synchronized (jSche) {
					jSche.notify();
				}
			}
		});

		jSche = JSche.start();

		synchronized (jSche) {
			try {
				jSche.wait();
			} catch (InterruptedException e) {
				log.info("Application has been terminated");
				log.debug("Application termination cause: ", e);
			}
		}
	}

	private static void setEventsClasses() {
		EventsClassesHolder.getInstance().setClasses(JScheTestEvent.class);
	}

}
