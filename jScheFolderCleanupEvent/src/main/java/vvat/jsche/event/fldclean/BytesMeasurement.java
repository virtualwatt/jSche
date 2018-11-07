package vvat.jsche.event.fldclean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class BytesMeasurement {
	
	public enum SizeSpec {
		B, KB, MB, GB
	}
	
	@XmlAttribute(name="in", required=false)
	private SizeSpec sizeSpec = SizeSpec.B;

	@XmlValue
	private long size;

	public SizeSpec getSizeSpec() {
		return sizeSpec;
	}

	public void setSizeSpec(SizeSpec sizeSpec) {
		this.sizeSpec = sizeSpec;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
	
	public long getSizeInBytes() {
		long sizeInBytes;
		switch (sizeSpec) {
		case B:
			sizeInBytes = size;
			break;
		case KB:
			sizeInBytes = size * 1024;
			break;
		case MB:
			sizeInBytes = size * 1024 * 1024;
			break;
		case GB:
			sizeInBytes = size * 1024 * 1024 * 1024;
			break;
		default:
			throw new RuntimeException("Not supported sizeSpec: " + sizeSpec);
		}
		return sizeInBytes;
	}

}
