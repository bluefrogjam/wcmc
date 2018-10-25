/*
 * Created on Nov 15, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.provider;

import java.util.Properties;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.exception.FactoryException;
import edu.ucdavis.genomics.metabolomics.util.config.Configable;
import edu.ucdavis.genomics.metabolomics.util.config.xml.XMLConfigable;
import edu.ucdavis.genomics.metabolomics.util.io.source.Source;

/**
 * is used to create SampleDataProvider
 * @author wohlgemuth
 * @version Nov 15, 2005
 *
 */
public abstract class SampleDataProviderFactory {

    /**
     * name of the property
     */
    public static final String DEFAULT_PROPERTY_NAME = SampleDataProviderFactory.class.getName();

    private static String foundFactory = null;

    /**
     * returns an new instance of the factory
     * 
     * @author wohlgemuth
     * @version Nov 9, 2005
     * @return
     */
    public static SampleDataProviderFactory newInstance() {

        // Locate Factory
        foundFactory = findFactory(
                DEFAULT_PROPERTY_NAME,
                SmartSampleDataProviderFactory.class.getName());
        
        return newInstance(foundFactory);
    }


    /**
     * returns an new instance of the specified factory
     * 
     * @param factoryClass the specified factory to use
     * @author wohlgemuth
     * @version Nov 9, 2005
     * @return
     */
    public static SampleDataProviderFactory newInstance(String factoryClass) {
        Class classObject;
        SampleDataProviderFactory factory;

        try {
            classObject = Class.forName(factoryClass);
            factory = (SampleDataProviderFactory) classObject.newInstance();
            return factory;

        } catch (Exception e) {
            throw new FactoryException(e);
        }
    }

    

    /**
     * creates our provider
     * @author wohlgemuth
     * @version Nov 15, 2005
     * @param source the source to use
     * @return
     * @throws ConfigurationException 
     */
    public abstract SampleDataProvider createProvider(Source source) throws ConfigurationException;

    /**
     * finds our factory
     * 
     * @author wohlgemuth
     * @version Nov 9, 2005
     * @param property
     * @param defaultValue
     * @return
     */
    private static String findFactory(String property, String defaultValue) {
        String factory;

        // Check System Property
        factory = System.getProperty(property);
        if (factory == null) {
            // Check Configurator
            try {
                XMLConfigable config = Configable.CONFIG;
                Properties p = config.getConfigProvider().getProperties();
                factory = p.getProperty(property);
            } catch (Exception e) {
            }
        }

        if (factory == null) {
            factory = defaultValue;
        }

        return factory;
    }
}
