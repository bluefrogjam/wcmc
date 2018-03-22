/*
 * Created on Jan 20, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.io.dest;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;

import java.util.Map;

public class ByteArrayDestinationFactoryImpl extends DestinationFactory{

	public ByteArrayDestinationFactoryImpl() {
		super();
	}

	@Override
	public ByteArrayDestination createDestination(Object identifier, Map<?, ?> propertys) throws ConfigurationException {
		return new ByteArrayDestination();
	}

}
