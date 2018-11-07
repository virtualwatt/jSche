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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import vvat.jsche.core.scheduleconfig.ConfigCalculator;
import vvat.jsche.core.scheduleconfig.DayOfWeek;
import vvat.jsche.core.scheduleconfig.JScheEvent;

/**
 * @author Vitaliy Tkachenko
 */
class JScheExecutor implements Runnable {
	
	private static final Logger log = Logger.getLogger(JScheExecutor.class);
	
	JScheEvent event;
	
	ScheduledExecutorService scheduleService = Executors.newScheduledThreadPool(1);
	List<ScheduledFuture<?>> scheduledFutures = new ArrayList<ScheduledFuture<?>>(20);
	
	JScheExecutor(JScheEvent event) {
		this.event = event;
	}
	
	void cancelExecutors() {
		for (ScheduledFuture<?> scheduleAtFixedRate: scheduledFutures)
			scheduleAtFixedRate.cancel(true);
		scheduledFutures.clear();
		scheduleService.shutdown();
		log.info(event.toString() + ": executors canceled");
	}
	
	private static final SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	void addExecutor(DayOfWeek dayOfWeek, String time, String timeZone) {
		if (time == null || time.isEmpty()) {
			// Single execution, no schedule
			log.info(event.toString() + ": single execution");
			run();
			return;
		}
		long now = System.currentTimeMillis();
		long timeOfNextDay = ConfigCalculator.timeOfNextDay(now, dayOfWeek, time, timeZone);
		boolean eachDay = dayOfWeek == DayOfWeek.NULL;
		ScheduledFuture<?> scheduledFuture = scheduleService.scheduleAtFixedRate(
				this,
				timeOfNextDay - now,
				eachDay ? ConfigCalculator.MS_IN_DAY : ConfigCalculator.MS_IN_WEEK,
				TimeUnit.MILLISECONDS);
		scheduledFutures.add(scheduledFuture);
		String dateStr = logDateFormat.format(timeOfNextDay);
		StringBuilder sb = new StringBuilder();
		sb.append('{').append(event.toString()).append("} scheduled for each ").append(eachDay ? "day" : "week").append(" on [");
		if (!eachDay) {
			sb.append(dayOfWeek).append(", ");
		}
		sb.append(time).append("]{").append(timeZone).append("} starting at [").append(dateStr).append("](local time {");
		sb.append(TimeZone.getDefault().getID()).append("})");
		log.info(sb.toString());
	}

	@Override
	public void run() {
		try {
			event.execute();
		} catch (Throwable e) {
			log.error(event.toString() + ": error executing event", e);
		}
	}

	JScheEvent getEvent() {
		return event;
	}
	
	@Override
	public String toString() {
		return event == null ? "Executor with unset event" : "Executor for: " + event.toString();
	}
}
