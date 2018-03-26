/*
 * Created on Jan 20, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.io.dest;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * writes the data into a byte array
 * @author wohlgemuth
 * @version Jan 20, 2006
 *
 */
public class ByteArrayDestination implements Destination {
	ByteArrayOutputStream stream;

	public ByteArrayDestination() {
		super();
	}

	public OutputStream getOutputStream() throws IOException {
		stream = new ByteArrayOutputStream();
		return stream;
	}

	public void setIdentifier(Object o) throws ConfigurationException {
		throw new ConfigurationException("not supported");
	}

	public void configure(Map<?, ?> p) throws ConfigurationException {
		throw new ConfigurationException("not supported");
	}

	/**
	 * our data
	 * 
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @return
	 */
	public byte[] getBytes() {
		return stream.toByteArray();
	}

}
