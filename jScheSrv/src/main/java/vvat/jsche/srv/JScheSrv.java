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
package vvat.jsche.srv;

import org.apache.log4j.Logger;

import vvat.jsche.core.JSche;
import vvat.jsche.core.JScheSettings;

/**
 * @author Vitaliy Tkachenko
 */
public class JScheSrv
{
	private static final Logger log = Logger.getLogger(JScheSrv.class);
	
	/**
	 * To start scheduler in console mode
	 * @param args not used
	 */
	public static void main(String[] args)
    {
		Runtime.getRuntime().addShutdownHook(new Thread("breaker") {
			public void run() {
				JScheSrv.stop(new String[] {});
				// Let the service to finish normally
				synchronized (jSche) {
					try {
						jSche.wait();
					} catch (InterruptedException e) {
						log.warn("Abnormal service termination");
					}
				}
			}
		});
		start(args);
		synchronized (jSche) {
			jSche.notifyAll();
		}
    }

	private static JSche jSche;
	
	/**
	 * Start the scheduler service by Apache Commons Daemon windows service runner
	 * Per ACD requirement function must not end until the service is running
	 * @param args not used
	 */
	public static void start(String[] args) {
		JScheSettings.getInstance().setConfigSource("jSche.properties");
		jSche = JSche.start();
		synchronized (jSche) {
			try {
				jSche.wait();
			} catch (InterruptedException e) {
				log.info("Scheduler has been interrupted");
				log.debug("Scheduler interruption cause: ", e);
			}
		}
		log.info("jSche service has successfully ended");
	}

	/**
	 * Stop the scheduler service
	 * @param args not used
	 */
	public static void stop(String[] args) {
		if (jSche != null) {
			jSche.stop();
			synchronized (jSche) {
				jSche.notifyAll();
			}
		} else {
			log.warn("Can't stop the scheduler: no service found");
		}
	}
}
