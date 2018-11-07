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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

/**
 * @author Vitaliy Tkachenko
 */
class Rescheduler implements Runnable {
	
	private static final Logger log = Logger.getLogger(Rescheduler.class);
	
	private BlockingQueue<ReschedulingEvent> reschedulingQueue;
	private Map<String, Schedule> schedules = new HashMap<String, Schedule>();

	Rescheduler(BlockingQueue<ReschedulingEvent> reschedulingQueue) {
		this.reschedulingQueue = reschedulingQueue;
	}

	@Override
	public void run() {
		try {
			while (true) {
				ReschedulingEvent event = reschedulingQueue.take();
				reconfigure(event);
			}
		} catch (InterruptedException e) {
			log.info("Rescheduler has been terminated");
			log.debug("Rescheduler termination cause", e);
		}
		cancelAll();
	}

	private void reconfigure(ReschedulingEvent event) {
		String configFile = event.getConfigFile();
		Schedule schedule = schedules.get(configFile);
		if (event.isRemove()) {
			schedules.remove(configFile);
			if (schedule != null)
				schedule.cancel();
			if (log.isDebugEnabled())
				log.debug("Schedule canceled " + schedule.toString());
		} else {
			if (schedule == null) {
				schedule = new Schedule(configFile);
				schedules.put(configFile, schedule);
				if (log.isDebugEnabled())
					log.debug("Added new schedule " + schedule.toString() + " total schedules: " + schedules.size());
			}
			schedule.update();
			if (log.isDebugEnabled())
				log.debug("Schedule updated " + schedule.toString());
		}
	}
	
	void cancelAll() {
		Iterator<Entry<String, Schedule>> it = schedules.entrySet().iterator();
		while (it.hasNext()) {
			Schedule schedule = it.next().getValue();
			schedule.cancel();
		}
		schedules.clear();
	}
}
