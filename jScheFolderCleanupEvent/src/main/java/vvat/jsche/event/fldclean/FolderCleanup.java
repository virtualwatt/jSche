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
package vvat.jsche.event.fldclean;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import vvat.jsche.core.scheduleconfig.JScheEvent;

/**
 * @author Vitaliy Tkachenko
 */
@XmlRootElement
public class FolderCleanup extends JScheEvent {

	private static final Logger log = Logger.getLogger(FolderCleanup.class);

	private static final int DEFAULT_FOLDER_SIZE = 100;
	
	private String folder;
	
	private boolean includeSubfolders;
	
	private BytesMeasurement maximumSize;

	private List<File> files;
	private long totalSize;
	
	private final static Comparator<File> comparator = new Comparator<File>() {
	    public int compare(File f1, File f2) {
	        return Long.compare(f1.lastModified(), f2.lastModified());
	    }
	};

	@Override
	public void execute() {
		log.info(getFolder() + ": folder cleanup execution");
		File theFolder = new File (getFolder());
		files = new ArrayList<File>(DEFAULT_FOLDER_SIZE);
		totalSize = 0;
		listFilesInFolder(theFolder);
		File[] sortedFiles = files.toArray(new File[0]);
		Arrays.sort(sortedFiles, comparator);
		long maxSize = maximumSize.getSizeInBytes();
		if (totalSize > maxSize) {
			long toDelete = totalSize - maxSize;
			long deletedSize = 0;
			for (File file: sortedFiles) {
				String name = file.getAbsolutePath();
				long fileSize = file.length();
				try {
					if (file.delete()) {
						deletedSize += fileSize;
						log.info(name + ": removed");
					} else {
						log.warn(name + ": can't remove");
					}
				}
				catch (SecurityException e) {
					log.warn(name + ": can't remove", e);
				}
				if (deletedSize >= toDelete)
					break;
			}
		}
		log.info(getFolder() + ": folder cleanup finished");
	}

	private void listFilesInFolder(File theFolder) {
		File[] folderList = theFolder.listFiles();
		for (File listItem: folderList) {
			if (listItem.isFile()) {
				files.add(listItem);
				totalSize += listItem.length();
			} else if (includeSubfolders && listItem.isDirectory())
				listFilesInFolder(listItem);
		}
	}

	@Override
	public boolean assertValid() {
		if (getFolder() == null || getFolder().isEmpty()) {
			log.error("FolderCleanup: it is mandatory to specify the folder to cleanup");
			return false;
		}
		if (!new File(getFolder()).isDirectory()) {
			log.error(getFolder() + ": no such directory");
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return getFolder() + " directory cleaner";
	}

	public boolean isIncludeSubfolders() {
		return includeSubfolders;
	}

	public void setIncludeSubfolders(boolean includeSubfolders) {
		this.includeSubfolders = includeSubfolders;
	}

	public BytesMeasurement getMaximumSize() {
		return maximumSize;
	}

	public void setMaximumSize(BytesMeasurement maximumSize) {
		this.maximumSize = maximumSize;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}
}
