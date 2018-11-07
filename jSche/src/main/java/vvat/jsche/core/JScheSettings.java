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

import java.io.InputStream;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.log4j.Logger;

/**
 * @author Vitaliy Tkachenko
 */
public class JScheSettings {

	private static final Logger log = Logger.getLogger(JScheSettings.class);
	
	private static final JScheSettings singletone = new JScheSettings();

	private static final String CONFIG_FILE_EXT = "vvat.jsche.core.configFileExt";
	private static final String DEFAULT_TIME_ZONE = "vvat.jsche.core.defaultTimeZone";
	private static final String FILE_EVENT_DELAY = "vvat.jsche.core.fileEventDelay";
	private static final String EVENTS_CLASS_HOLDER= "vvat.jsche.core.eventsClassHolder";

	private String defaultTimeZone;
	private String configFileExt;
	private long fileEventDelay;

	private String eventsClassHolder;

	private Properties properties = null;

	private static final String CONFIG_FILE_EXT_DEFAULT = ".jSche.xml";
	private static final long FILE_EVENT_DELAY_DEFAULT = 1000;
	private static final String EVENTS_CLASS_HOLDER_DEFAULT = "eventsClassesHolder.xml";
	
	public static JScheSettings getInstance() {
		return singletone;
	}
	
	/**
	 * Load configuration from properties file (regular method).
	 * Must be set prior to service run.
	 * 
	 * @param propertiesFile
	 */
	public void setConfigSource(String propertiesFile) {
		Properties prop = new Properties();
		ClassLoader classLoader = JScheSettings.class.getClassLoader();
		try {
			prop.load(classLoader.getResourceAsStream(propertiesFile));
			this.properties = prop;
		} catch (Throwable e) {
			log.error("Unable to read " + propertiesFile, e);
		}
	}
	
	/**
	 * Load configuration from properties object (alternative method)
	 * 
	 * @param properties
	 */
	public void setConfigSource(Properties properties) {
		this.properties = properties;
	}

	/**
	 * Load configuration from input stream
	 * 
	 * @param propertiesInputStream
	 */
	public void setConfigSource(InputStream propertiesInputStream) {
		Properties prop = new Properties();
		try {
			prop.load(propertiesInputStream);
			this.properties = prop;
		} catch (Throwable e) {
			log.error("Unable to read " + propertiesInputStream, e);
		}
	}

	void init() {
		
		if (properties == null) {
			defaultTimeZone = TimeZone.getDefault().getID();
			configFileExt = CONFIG_FILE_EXT_DEFAULT;
			fileEventDelay = FILE_EVENT_DELAY_DEFAULT;
			eventsClassHolder = EVENTS_CLASS_HOLDER_DEFAULT;
			return;
		}
		
		String timeZone = properties.getProperty(DEFAULT_TIME_ZONE);
		defaultTimeZone = timeZone == null || timeZone.isEmpty() ? TimeZone.getDefault().getID() : timeZone;
		configFileExt = properties.getProperty(CONFIG_FILE_EXT);
		eventsClassHolder = properties.getProperty(EVENTS_CLASS_HOLDER);
		long delay = FILE_EVENT_DELAY_DEFAULT;
		try {
			delay = Long.parseLong(properties.getProperty(FILE_EVENT_DELAY));
		} catch (NumberFormatException e) {
			log.error("Wrong parameter " + FILE_EVENT_DELAY, e);
		}
		fileEventDelay = delay;
	}

	public String getDefaultTimeZone() {
		return defaultTimeZone;
	}

	public String getConfigFileExt() {
		return configFileExt;
	}

	public long getFileEventDelay() {
		return fileEventDelay;
	}

	public String getEventsClassHolder() {
		return eventsClassHolder;
	}
}
