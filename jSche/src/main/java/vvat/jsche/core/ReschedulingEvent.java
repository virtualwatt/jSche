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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author Vitaliy Tkachenko
 */
class ReschedulingEvent implements Delayed {
	
	//private static final Logger log = Logger.getLogger(ReschedulingEvent.class);
	
	private long eventGenerationTime;
	private long delay;

	private String configFile;
	private boolean remove;

	private final void init(String configFile, boolean remove, long delay) {
		this.configFile = configFile;
		this.remove = remove;
		this.delay = delay;
		eventGenerationTime = System.currentTimeMillis();
	}
	
	ReschedulingEvent(String configFile, boolean remove) {
		init(configFile, remove, JScheSettings.getInstance().getFileEventDelay());
	}
	
	ReschedulingEvent(String configFile, boolean remove, long delay) {
		init(configFile, remove, delay);
	}
	
	String getConfigFile() {
		return configFile;
	}

	boolean isRemove() {
		return remove;
	}
	
	@Override
	public boolean equals(Object toCompare) {
		if (this == toCompare)
			return true;
		if (!(toCompare instanceof ReschedulingEvent) || configFile == null)
			return false;
		ReschedulingEvent that = (ReschedulingEvent)toCompare;
		return configFile.equals(that.configFile);
	}

	@Override
	public int hashCode() {
		return configFile == null ? 0 : configFile.hashCode();
	}

	@Override
	public int compareTo(Delayed toCompare) {
		if (this == toCompare)
			return 0;
		if (!(toCompare instanceof ReschedulingEvent) || configFile == null)
			return Integer.MIN_VALUE;
		ReschedulingEvent that = (ReschedulingEvent)toCompare;
		return configFile.compareTo(that.configFile);
	}

	@Override
	public long getDelay(TimeUnit timeUnit) {
		// The function must be ready any time to return the delay left until the event can be taken
		if (eventGenerationTime == 0)
			return 0;
		long toDelayFor = (eventGenerationTime + delay) - System.currentTimeMillis();
		return timeUnit.convert(toDelayFor, TimeUnit.MILLISECONDS);
	}
}
