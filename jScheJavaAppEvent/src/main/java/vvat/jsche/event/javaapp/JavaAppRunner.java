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
package vvat.jsche.event.javaapp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import vvat.jsche.core.scheduleconfig.JScheEvent;

/**
 * @author Vitaliy Tkachenko
 */
@XmlRootElement
public class JavaAppRunner extends JScheEvent {

	private static final Logger log = Logger.getLogger(JavaAppRunner.class);
	
	private String startClass;
	private String[] argument;

	public void setStartClass(String startClass) {
		this.startClass = startClass;
	}

	public String getStartClass() {
		return startClass;
	}

	public void setArgument(String[] argument) {
		this.argument = argument;
	}
	
	public String[] getArgument() {
		return argument;
	}

	@Override
	public void execute() {
		log.info(startClass + ": java application execution");
		Class<?> cls;
		try {
			cls = Class.forName(startClass);
		} catch (ClassNotFoundException e) {
			log.error(startClass + ": no such class found");
			return;
		}
	    Method method;
		try {
			method = cls.getMethod("main", String[].class);
		} catch (NoSuchMethodException e) {
			log.error(startClass + ": class doesn't contain main method");
			return;
		} catch (SecurityException e) {
			log.error(startClass + ": security violation while main method retrieval");
			e.printStackTrace();
			return;
		}
	    try {
			method.invoke(null, (Object)(argument == null ? new String[0] : argument));
		} catch (IllegalAccessException e) {
			log.error(startClass + ": main method access violation");
			return;
		} catch (IllegalArgumentException e) {
			log.error(startClass + ": illegal argument passed to the main method");
			return;
		} catch (InvocationTargetException e) {
			log.error(startClass + ": main method invocation failure");
			return;
		}
		log.info(startClass + ": java application execution has successfully ended");
	}

	@Override
	public boolean assertValid() {
		if (startClass == null) {
			log.error("JavaAppRunner: it is mandatory to specify the class to run");
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return startClass + " java application runner";
	}
}
