/*
 * Created on Nov 15, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.provider;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;

public class PegasusASCIISampleDataProviderFactoryImpl extends SampleDataProviderFactory{

	public SampleDataProvider createProvider(Source source) throws ConfigurationException {
		SampleDataProvider p = new PegasusASCIIIProvider();
		p.setSource(source);
		return p;
	}

}
