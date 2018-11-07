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
package vvat.jsche.event.procexec;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import vvat.jsche.core.scheduleconfig.JScheEvent;

/**
 * @author Vitaliy Tkachenko
 */
@XmlRootElement
public class ProcessExecutor extends JScheEvent {

	private static final Logger log = Logger.getLogger(ProcessExecutor.class);
	
	private String cmdLine;
	private String[] cmdLinePart;
	private String[] envVar;
	private String curDir;

	public String getCmdLine() {
		return cmdLine;
	}

	public void setCmdLine(String cmdLine) {
		this.cmdLine = cmdLine;
	}

	public void setCmdLinePart(String[] cmdLine) {
		this.cmdLinePart = cmdLine;
	}

	public String[] getCmdLinePart() {
		return cmdLinePart;
	}

	public void setEnvVar(String[] envVar) {
		this.envVar = envVar;
	}
	
	public String[] getEnvVar() {
		return envVar;
	}

	public void setCurDir(String curDir) {
		this.curDir = curDir;
	}

	public String getCurDir() {
		return curDir;
	}

	@Override
	public void execute() {
		log.info(cmdLine + " |=> process execution");
		try {
			Runtime runtime = Runtime.getRuntime();
			Process proc;
			File currentDir = curDir == null ? null : new File(curDir);
			if (cmdLinePart == null || cmdLinePart.length == 1)
				proc = runtime.exec(cmdLine, envVar, currentDir);
			else
				proc = runtime.exec(cmdLinePart, envVar, currentDir);
			final BufferedReader procOutStream = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			final BufferedReader procErrStream = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			new Thread(new Runnable() {
				@Override
				public void run() {
					String line;
					try {
						while ((line = procOutStream.readLine()) != null) {
							System.out.println(line);
						}
						procOutStream.close();
					} catch (IOException e) {
						log.error(cmdLine + " |=> error occurred reading process standard output stream", e);
					}
				}
			}).start();
			new Thread(new Runnable() {
				@Override
				public void run() {
					String line;
					try {
						while ((line = procErrStream.readLine()) != null) {
							System.err.println(line);
						}
						procErrStream.close();
					} catch (IOException e) {
						log.error(cmdLine + " |=> error occurred reading process standard error stream", e);
					}
				}
			}).start();
			try {
				proc.waitFor();
			} catch (InterruptedException e) {
				log.error(cmdLine + " |=> process has been interrupted", e);
			}
			log.info(cmdLine + " |=> process execution has ended with exit code " + proc.exitValue());
		} catch (IOException e) {
			log.error(cmdLine + " |=> error occurred running process", e);
		}
	}

	@Override
	public boolean assertValid() {
		if (cmdLine == null) {
			if (cmdLinePart == null || cmdLinePart.length == 0) {
				log.error("ProcessExecutor: it is mandatory to specify the command line");
				return false;
			}
			StringBuilder stringBuilder = new StringBuilder();
			int length = cmdLinePart.length;
			for (int i = 0; i < length; i++) {
				String clp = cmdLinePart[i];
				stringBuilder.append(clp);
				if (i < length - 1)
					stringBuilder.append(' ');
			}
			cmdLine = stringBuilder.toString();
		}
		return true;
	}

	@Override
	public String toString() {
		return cmdLine + " |=> launcher";
	}
}
