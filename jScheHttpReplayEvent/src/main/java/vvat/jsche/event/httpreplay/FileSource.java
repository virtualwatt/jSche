/**
 * 
 */
package vvat.jsche.event.httpreplay;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author vtkachenko
 *
 */
public class FileSource {

	private String fileName;
	
	private String folderName;

	private ByteArrayData fileData;
	
	public FileSource() {;}
	
	public FileSource(String fileName, ByteArrayData fileData) {
		this.fileName = fileName;
		this.fileData = fileData;
	}
	
	public FileSource(String fileName, String folderName, ByteArrayData fileData) {
		this(fileName, fileData);
		this.folderName = folderName;
	}

	public FileSource(String fromFile) throws IOException {
		setFileSource(fromFile);
	}

	public FileSource(String fromFile, String folderName) throws IOException {
		setFileSource(fromFile);
		this.folderName = folderName;
	}

	public void setFileSource(String fromFile) throws IOException {
		File file = new File(fromFile);
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			fileData = readStreamToByteArray(in);
			int lastDash = fromFile.lastIndexOf('\\');
			fileName = lastDash == -1 ? fromFile : fromFile.substring(lastDash + 1);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public ByteArrayData getFileData() {
		return fileData;
	}

	public void setFileData(ByteArrayData fileData) {
		this.fileData = fileData;
	}
	
	public InputStream getInputStream() {
		return fileData.getInputStream();
	}
	
	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

    private static final int BUFFER_SIZE = 16384;

    public static ByteArrayData readStreamToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[BUFFER_SIZE];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
          buffer.write(data, 0, nRead);
        }
        buffer.flush();

        ByteArrayData byteArrayData = new ByteArrayData();
        byteArrayData.setContent(buffer.toByteArray());
        return byteArrayData;
    }
}
