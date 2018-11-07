package vvat.jsche.core.scheduleconfig;

import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder={"timeZone", "dayOfWeek", "time", "event", "eventRef"})
public class JScheConfig {

	private List<DayOfWeek> dayOfWeek;
	private List<String> time;
	private String timeZone;
	
	private JScheEvent event;
	private JScheEventRef eventRef;

	
	public List<DayOfWeek> getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(List<DayOfWeek> dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public List<String> getTime() {
		return time;
	}

	public void setTime(List<String> time) {
		this.time = time;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	@XmlElementRef
	public JScheEvent getEvent() {
		return event;
	}

	public void setEvent(JScheEvent event) {
		this.event = event;
	}

	public JScheEventRef getEventRef() {
		return eventRef;
	}

	public void setEventRef(JScheEventRef eventRef) {
		this.eventRef = eventRef;
	}
}
