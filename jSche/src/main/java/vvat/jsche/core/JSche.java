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

import java.util.concurrent.DelayQueue;

import org.apache.log4j.Logger;

/**
 * @author Vitaliy Tkachenko
 */
public class JSche
{
	private static final Logger log = Logger.getLogger(JSche.class);

	private static JSche thisSingletone;
	
	private DelayQueue<ReschedulingEvent> reschedulingQueue;
	private Rescheduler rescheduler;
	private Thread reschedulerThread;
	private DirWatcher dirWatcher;
	private Thread dirWatcherThread;

	private JSche() {
		reschedulingQueue = new DelayQueue<ReschedulingEvent>();
		rescheduler = new Rescheduler(reschedulingQueue);
		dirWatcher = new DirWatcher(reschedulingQueue);
		reschedulerThread = new Thread(rescheduler, "rescheduler");
		dirWatcherThread = new Thread(dirWatcher, "dirWatcher");
	}
	
	private void startSchedulersService() {
		dirWatcher.initSchedulers();
		reschedulerThread.start();
		dirWatcherThread.start();
	}
	
	private void stopSchedulersService() {
		dirWatcherThread.interrupt();
		try {
			dirWatcherThread.join();
		} catch (InterruptedException e) {
			log.warn("Dir watcher abnormal termination", e);
		}
		reschedulerThread.interrupt();
		try {
			reschedulerThread.join();
		} catch (InterruptedException e) {
			log.warn("Rescheduler abnormal termination", e);
		}
	}
	
	/**
	 * Start the Java Simple Scheduler (jSche)
	 *
	 * @return reference to singletone to this class
	 */
	public static JSche start() {
		log.info("Starting the scheduler");
		JScheSettings.getInstance().init();
		EventsClassesHolder.getInstance().init();
		thisSingletone = new JSche();
		thisSingletone.startSchedulersService();
		return thisSingletone;
	}

	/**
	 * Stop the Java Simple Scheduler (jSche)
	 * 
	 */
	public void stop() {
		log.info("Stopping the scheduler");
		stopSchedulersService();
		log.info("The scheduler has successfully ended");
	}
}
