/*
 * Created on Jan 20, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.io.source;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;

import java.util.Map;

public class ByteArraySourceFactoryImpl extends SourceFactory {

	public ByteArraySourceFactoryImpl() {
		super();
	}

	@Override
	public ByteArraySource createSource(Object identifier, Map<?, ?> propertys) throws ConfigurationException {
		if (identifier instanceof byte[]) {
			return new ByteArraySource((byte[]) identifier);
		} else {
			return new ByteArraySource(identifier.toString().getBytes());
		}
	}

}
