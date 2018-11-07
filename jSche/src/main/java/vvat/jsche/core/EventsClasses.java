package vvat.jsche.core;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EventsClasses {
	
	private List<String> eventClass = new ArrayList<String>();

	public List<String> getEventClass() {
		return eventClass;
	}

	public void setEventClass(List<String> eventClass) {
		this.eventClass = eventClass;
	}

}
