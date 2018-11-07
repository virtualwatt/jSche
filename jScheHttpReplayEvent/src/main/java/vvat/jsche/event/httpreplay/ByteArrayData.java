/**
 * 
 */
package vvat.jsche.event.httpreplay;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * @author vtkachenko
 *
 */
public class ByteArrayData implements Serializable {

	private static final long serialVersionUID = 1L;

	private byte[] content;

	public ByteArrayData() {}

	public ByteArrayData(byte[] content) {
		this.content = content;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public int getContentLength() {
		return content.length;
	}

	public InputStream getInputStream() {
		return new ByteArrayInputStream(content, 0, content.length);
	}
	
	@Override
	public String toString() {
		return new String(content, 0, content.length);
	}

	public String toString(String encoding) throws UnsupportedEncodingException {
		if (encoding == null || encoding.isEmpty()) {
			encoding = StandardCharsets.UTF_8.toString();
		}
		return new String(content, 0, content.length, encoding);
	}
}
