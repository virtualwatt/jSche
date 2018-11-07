package vvat.jsche.core;

import javax.xml.bind.annotation.XmlRootElement;

import vvat.jsche.core.scheduleconfig.JScheEvent;

@XmlRootElement
public class TestEventClassTest extends JScheEvent {

	@Override
	public boolean assertValid() {
		return true;
	}

	@Override
	public void execute() {}

}
