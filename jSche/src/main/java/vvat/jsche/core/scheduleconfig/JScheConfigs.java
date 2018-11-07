package vvat.jsche.core.scheduleconfig;

import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder={"timeZone", "jScheConfig", "event"})
public class JScheConfigs {

	private String timeZone;
	private List<JScheConfig> jScheConfig;
	private List<JScheEvent> event;
	
	public List<JScheConfig> getjScheConfig() {
		return jScheConfig;
	}

	public void setjScheConfig(List<JScheConfig> jScheConfig) {
		this.jScheConfig = jScheConfig;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	@XmlElementRef
	public List<JScheEvent> getEvent() {
		return event;
	}

	public void setEvent(List<JScheEvent> event) {
		this.event = event;
	}
	
}
