/*
 * Created on Jan 20, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.io.source;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * uses a byte array as source
 * @author wohlgemuth
 * @version Jan 20, 2006
 *
 */
public class ByteArraySource implements Source{
	private byte bytes[] = null;
	
	public ByteArraySource(byte[] bytes) {
		super();
		this.bytes = bytes;
	}

	/**
	 * 
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @see Source#getStream()
	 */
	public InputStream getStream() throws IOException {
		return new ByteArrayInputStream(this.bytes);
	}

	/**
	 * the name of the source
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @see Source#getSourceName()
	 */
	public String getSourceName() {
		// TODO Auto-generated method stub
		return String.valueOf(this.bytes.hashCode());
	}

	/**
	 * 
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @see Source#setIdentifier(Object)
	 */
	public void setIdentifier(Object o) throws ConfigurationException {
		throw new ConfigurationException("not supported");
	}

	/**
	 * 
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @see Source#configure(Map)
	 */
	public void configure(Map<?, ?> p) throws ConfigurationException {
		throw new ConfigurationException("not supported");
	}

	/**
	 * 
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @see Source#exist()
	 */
	public boolean exist() {
		return bytes != null;
	}

	public long getVersion() {
		return 0;
	}

}
