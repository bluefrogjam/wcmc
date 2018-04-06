package edu.ucdavis.genomics.metabolomics.binbase.minix.wsdl;

import edu.ucdavis.genomics.metabolomics.binbase.bci.setupX.SetupXProvider;

import java.util.Properties;

/**
 * Created by wohlgemuth on 10/19/16.
 */
public class MiniXProviderFactory extends SetupXFactory {
    @Override
    public SetupXProvider createProvider(Properties p) {
        return new MiniXProvider("http://minix.fiehnlab.ucdavis.edu/services/communications?wsdl");
    }
}
