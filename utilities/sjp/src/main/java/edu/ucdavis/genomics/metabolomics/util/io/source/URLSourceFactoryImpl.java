/*
 * Created on Jan 20, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.io.source;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;

import java.util.Map;

public class URLSourceFactoryImpl extends SourceFactory {

	public URLSourceFactoryImpl() {
		super();
		
	}

	@Override
	public URLSource createSource(Object identifier, Map<?, ?> propertys) throws ConfigurationException {
		URLSource source = new URLSource();
		source.setIdentifier(identifier);
		source.configure(propertys);
		return source;
	}

}
