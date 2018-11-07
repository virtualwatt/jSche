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
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

/**
 * @author Vitaliy Tkachenko
 */
class DirWatcher implements Runnable {
	
	private static final Logger log = Logger.getLogger(DirWatcher.class);

	private WatchService watcher;
	private BlockingQueue<ReschedulingEvent> reschedulingQueue;

	private String configDir;
	
	DirWatcher(BlockingQueue<ReschedulingEvent> reschedulingQueue) {
		this.reschedulingQueue = reschedulingQueue;
	}
	
	@Override
	public void run() {
		if (configDir == null)
			return;
		Path myDir = Paths.get(configDir);
		try {
			watcher = myDir.getFileSystem().newWatchService();
			myDir.register(watcher, /*StandardWatchEventKinds.ENTRY_CREATE,*/
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
			loopWatcher();
		} catch (IOException e) {
			log.error("Error setting up watch service", e);
		}
	}

	private void loopWatcher() {
		try {
			while (true) {
				WatchKey watckKey = watcher.take();
				
				List<WatchEvent<?>> events = watckKey.pollEvents();
				log.debug("JScheSettings directory events detected");
				for (WatchEvent<?> event : events) {
					WatchEvent.Kind<?> kind = event.kind();
					if (kind == StandardWatchEventKinds.OVERFLOW) {
			            continue;
			        }
					ReschedulingEvent reschedulingEvent;
					String file = event.context().toString();
					if (file == null || !file.endsWith(JScheSettings.getInstance().getConfigFileExt()))
						continue;
					if (/*kind == StandardWatchEventKinds.ENTRY_CREATE ||*/
							kind == StandardWatchEventKinds.ENTRY_MODIFY) {
						if (log.isDebugEnabled())
							log.debug("JScheSettings (re)defined: " + file);
						reschedulingEvent = new ReschedulingEvent(file, false);
					} else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
						if (log.isDebugEnabled())
							log.debug("JScheSettings removed: " + file);
						reschedulingEvent = new ReschedulingEvent(file, true);
					} else
						continue;
					handleReschedulingEvent(reschedulingEvent);
				}
				boolean valid = watckKey.reset();
			    if (!valid) {
			    	log.warn("JScheSettings directory watcher has stopped due to a problem with the config directory");
			        break;
			    }
			}
		} catch (InterruptedException e) {
			log.info("JScheSettings directory watcher has been terminated");
			log.debug("JScheSettings directory watcher termination cause: ", e);
		}
	}
	
	private static FilenameFilter schedulersExtensionFileFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			if (name.endsWith(JScheSettings.getInstance().getConfigFileExt()))
		        return true;
			return false;
		}
	};
	
	void initSchedulers() {
		configDir = System.getProperty("configDir");
		if (configDir == null || configDir.isEmpty()) {
			configDir = null;
			log.fatal("Config dir not set. Set the configDir system property and restart the service");
			return;
		}
		String confDir = System.getProperty("configDir");
		if (confDir == null || confDir.isEmpty())
			throw new RuntimeException("Config directory (VM argument 'configDir') not specified");
		File configDirFile = new File(confDir);
		if (!configDirFile.isDirectory())
			throw new RuntimeException("Config directory (VM argument 'configDir') not found");
		String[] list = configDirFile.list(schedulersExtensionFileFilter);
		for (String file: list) {
			log.info("Initializing schedules for: " + file);
			try {
				handleReschedulingEvent(new ReschedulingEvent(file, false));
			} catch (InterruptedException e) {
				log.error(file + ": schedule initialization error", e);
			}
		}
		log.info("Schedulers initialization performed");
	}

	private void handleReschedulingEvent(ReschedulingEvent reschedulingEvent) throws InterruptedException {
		boolean removed = reschedulingQueue.remove(reschedulingEvent);
		reschedulingQueue.put(reschedulingEvent);
		if (log.isDebugEnabled())
			log.debug("Rescheduling event added [" + reschedulingEvent + "] (" +
					(removed ? "was" : "wasn't") +
					" previously removed), events in queue: " + reschedulingQueue.size());
	}

}
