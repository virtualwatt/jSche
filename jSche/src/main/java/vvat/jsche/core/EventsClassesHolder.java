/*
 *  Copyright 2014 Vitaliy Tkachenko virtualvat@gmail.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package vvat.jsche.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import vvat.jsche.core.scheduleconfig.JScheConfigs;
import vvat.jsche.core.scheduleconfig.JScheEvent;

/**
 * @author Vitaliy Tkachenko
 */
public class EventsClassesHolder {
	
	private static final Logger log = Logger.getLogger(EventsClassesHolder.class);
	
	private static final EventsClassesHolder instance = new EventsClassesHolder();
	
	private Class<?>[] classes;

	public static EventsClassesHolder getInstance() {
		return instance;
	}

	/**
	 * Specify all possible classes which events can be scheduled.
	 * This alternative (more simple) way to specify them.
	 * It is more flexible to specify classes using the XML defined by EventsClasses.
	 * 
	 * @param classes
	 */
	@SafeVarargs
	public final void setClasses(Class<? extends JScheEvent> ... classes) {
		this.classes = Arrays.copyOf(classes, classes.length + 1);
		this.classes[classes.length] = JScheConfigs.class;
	}
	
	/**
	 * Call to initialize events classes i.e. load them from the XML defined by EventsClasses.
	 * Must be called prior to service start. Not required if classes are set using setClasses method.
	 */
	public void init() {
		if (classes != null)
			return;
		
		EventsClasses eventsClasses;
		String configFile = JScheSettings.getInstance().getEventsClassHolder();
		try {
			JAXBContext jc = JAXBContext.newInstance(EventsClasses.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			eventsClasses = (EventsClasses)unmarshaller.unmarshal(
					EventsClassesHolder.class.getClassLoader().getResourceAsStream(configFile));
		} catch (JAXBException e) {
			log.error("Error unmarshalling " + configFile, e);
			return;
		}
		List<Class<?>> classList = new ArrayList<Class<?>>();
		classList.add(JScheConfigs.class);
		for (String cls: eventsClasses.getEventClass()) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends JScheEvent> theClass = (Class<? extends JScheEvent>)Class.forName(cls);
				if (JScheEvent.class.isAssignableFrom(theClass))
					classList.add(theClass);
				else
					log.error(cls + ": class does not extend JScheEvent");
			}
			catch (ClassNotFoundException e) {
				log.error(cls + ": class not found", e);;
			}
			catch (ClassCastException e) {
				log.error(cls + ": class does not extend JScheEvent");
			}
		}
		classes = classList.toArray(new Class[]{});
	}

	Class<?>[] getConfigClasses() {
		return classes;
	}
}
