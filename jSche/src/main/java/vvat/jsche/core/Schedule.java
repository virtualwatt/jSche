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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import vvat.jsche.core.scheduleconfig.DayOfWeek;
import vvat.jsche.core.scheduleconfig.JScheConfig;
import vvat.jsche.core.scheduleconfig.JScheConfigs;
import vvat.jsche.core.scheduleconfig.JScheEvent;
import vvat.jsche.core.scheduleconfig.JScheEventRef;

/**
 * @author Vitaliy Tkachenko
 */
class Schedule {
	
	private static final Logger log = Logger.getLogger(Schedule.class);

	private String configFile;
	private List<JScheExecutor> jScheExecutors = new ArrayList<JScheExecutor>(20);

	Schedule(String configFile) {
		this.configFile = configFile;
	}

	void update() {
		cancel();
		JScheConfigs jScheConfigs;
		try {
			JAXBContext jc = JAXBContext.newInstance(EventsClassesHolder.getInstance().getConfigClasses());
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			jScheConfigs = (JScheConfigs)unmarshaller.unmarshal(
					new File(System.getProperty("configDir") + '/' + configFile));
		} catch (JAXBException e) {
			log.error("Error unmarshalling " + configFile, e);
			return;
		}
		Map<String, JScheEvent> events = generateEventsMap(jScheConfigs.getEvent());
		log.info(configFile + ": setting up executors");
		for (JScheConfig jScheConfig: jScheConfigs.getjScheConfig()) {
			JScheEvent jScheEvent = jScheConfig.getEvent();
			if (jScheEvent == null) {
				JScheEventRef eventRef = jScheConfig.getEventRef();
				if (eventRef == null) {
					log.error(configFile + ": none of event or eventRef is set in a config, make sure all events extend JScheEvent and listed in \"events classes\"");
					continue;
				}
				String eventId = eventRef.getId();
				if (eventId == null) {
					log.error(configFile + ": eventRef ID is not set in a config, unable to process this event");
					continue;
				}
				jScheEvent = events.get(eventId);
				if (jScheEvent == null) {
					log.error(eventId + ": unable to find event with such ID");
					continue;
				}
			}
			if (!jScheEvent.assertValid()) {
				log.error("Skipping invalid event setup");
				continue;
			}
			JScheExecutor jScheExecutor = new JScheExecutor(jScheEvent);
			List<String> times = jScheConfig.getTime();
			List<DayOfWeek> daysOfWeek = jScheConfig.getDayOfWeek();
			String timeZone = jScheConfig.getTimeZone();
			if (timeZone == null || timeZone.isEmpty())
				timeZone = jScheConfigs.getTimeZone();
			if (timeZone == null || timeZone.isEmpty())
				timeZone = JScheSettings.getInstance().getDefaultTimeZone();
			try {
				if (daysOfWeek == null || daysOfWeek.isEmpty()) {
					if (times == null || times.isEmpty()) {
						// single execution
						jScheExecutor.addExecutor(DayOfWeek.NULL, null, null);
						jScheExecutor.cancelExecutors();
						continue;
					}
					// daily configuration
					for (String time: times) {
						jScheExecutor.addExecutor(DayOfWeek.NULL, time, timeZone);
					}
				} else {
					// weekly configuration
					for (DayOfWeek dayOfWeek: daysOfWeek) {
						for (String time: times) {
							jScheExecutor.addExecutor(dayOfWeek, time, timeZone);
						}
					}
				}
				jScheExecutors.add(jScheExecutor);
				if (log.isDebugEnabled())
					log.debug("Schedule.update: added executor: " + jScheExecutor);
			}
			catch (Exception e) {
				log.error("Schedule.update: error adding executor: " + jScheExecutor);
			}
		}
	}

	private Map<String, JScheEvent> generateEventsMap(List<JScheEvent> events) {
		Map<String, JScheEvent> map = new HashMap<String, JScheEvent>();
		if (events == null)
			return map;
		for (JScheEvent event: events) {
			String id = event.getId();
			if (id == null || id.isEmpty()) {
				log.error(configFile + ": config contains event with empty ID that is not allowed");
				continue;
			}
			map.put(id, event);
		}
		return map;
	}

	void cancel() {
		log.info(configFile + ": canceling all executors");
		for (JScheExecutor jScheExecutor: jScheExecutors) {
			jScheExecutor.cancelExecutors();
			log.info(jScheExecutor.getEvent().toString() + '@' + configFile + ": canceled");
		}
		jScheExecutors.clear();
	}

}
