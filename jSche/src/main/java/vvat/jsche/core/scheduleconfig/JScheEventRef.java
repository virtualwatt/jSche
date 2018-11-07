package vvat.jsche.core.scheduleconfig;

import javax.xml.bind.annotation.XmlAttribute;

public class JScheEventRef {

	private String id;
	
	@XmlAttribute
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
