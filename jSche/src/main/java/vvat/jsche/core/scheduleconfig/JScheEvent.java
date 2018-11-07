package vvat.jsche.core.scheduleconfig;

import javax.xml.bind.annotation.XmlAttribute;

public abstract class JScheEvent {
	
	private String id;
	
	public boolean assertValid() {
		return true;
	}
	
	public abstract void execute();

	public String getId() {
		return id;
	}

	@XmlAttribute
	public void setId(String id) {
		this.id = id;
	}

}
