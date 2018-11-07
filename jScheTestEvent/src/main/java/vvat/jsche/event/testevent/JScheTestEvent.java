package vvat.jsche.event.testevent;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import vvat.jsche.core.scheduleconfig.JScheEvent;

/**
 * @author Vitaliy Tkachenko
 */
@XmlRootElement
public class JScheTestEvent extends JScheEvent {
	
	private static final Logger log = Logger.getLogger(JScheTestEvent.class);

	@Override
	public void execute() {
		try {
			log.info("jSche test event: config folder: " + new File(System.getProperty("configDir")).getCanonicalPath());
		} catch (IOException e) {
			log.error("configuration directory anavailable", e);
		}
	}

	@Override
	public String toString() {
		return "JScheTestEvent: jSche test event";
	}
}
