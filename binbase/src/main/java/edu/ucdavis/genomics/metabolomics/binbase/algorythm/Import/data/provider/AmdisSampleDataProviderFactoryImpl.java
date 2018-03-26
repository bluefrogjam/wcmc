package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.provider;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.sjp.handler.CSVHandler;
import edu.ucdavis.genomics.metabolomics.sjp.parser.amdis.AmdisELUParser;
import edu.ucdavis.genomics.metabolomics.sjp.tools.ConvertAmdisToPegasus;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;

/**
 * Created by Gert on 4/8/2015.
 */
public class AmdisSampleDataProviderFactoryImpl extends SampleDataProviderFactory {
    @Override
    public SampleDataProvider createProvider(Source source) throws ConfigurationException {
        SampleDataProvider provider = new AmdisDataProvider();

        try {
            provider.setSource(source);
            return provider;
        } catch (Exception e) {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }
}
