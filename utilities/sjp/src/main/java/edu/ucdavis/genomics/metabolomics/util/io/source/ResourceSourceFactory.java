package edu.ucdavis.genomics.metabolomics.util.io.source;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;

import java.util.Map;

public class ResourceSourceFactory extends SourceFactory{

	@Override
	public Source createSource(Object identifier, Map<?, ?> propertys) throws ConfigurationException {
		return new ResourceSource(identifier.toString());
	}

}
