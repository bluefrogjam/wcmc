/*
 * Created on Nov 9, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.io.source;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import edu.ucdavis.genomics.metabolomics.exception.FactoryException;
import edu.ucdavis.genomics.metabolomics.util.AbstractFactory;

import java.util.Map;

/**
 * is used too create independent sources, base on the AbstractFactoryPattern
 * with help from
 * 
 * @author wohlgemuth
 * @version Nov 9, 2005
 * 
 */
public abstract class SourceFactory extends AbstractFactory{

    /**
     * name of the property
     */
    public static final String DEFAULT_PROPERTY_NAME = SourceFactory
    .class.getName();

    private static String foundFactory = null;

    /**
     * returns an new instance of the factory
     * 
     * @author wohlgemuth
     * @version Nov 9, 2005
     * @return
     */
    public static SourceFactory newInstance() {

        // Locate Factory
        foundFactory = findFactory(
                DEFAULT_PROPERTY_NAME,
                FileSourceFactoryImpl.class.getName());

        return newInstance(foundFactory);
    }


    /**
     * returns an new instance of the factory
     * 
     * @author wohlgemuth
     * @version Nov 9, 2005
     * @return
     */
    @SuppressWarnings("unchecked")
	public static SourceFactory newInstance(String factoryClass) {
        Class classObject;
        SourceFactory factory;

        try {
            classObject = Class.forName(factoryClass);
            factory = (SourceFactory) classObject.newInstance();
            return factory;

        } catch (Exception e) {
            throw new FactoryException(e);
        }
    }

    /**
     * creates a new source
     * 
     * @author wohlgemuth
     * @version Nov 9, 2005
     * @param identifier
     *            the identifer
     * @param the
     *            properties for the configuration of the source
     * @return
     * @throws ConfigurationException
     */
    public abstract Source createSource(Object identifier, Map<?, ?> propertys)
            throws ConfigurationException;

    /**
     * creates a new source
     * 
     * @author wohlgemuth
     * @version Nov 10, 2005
     * @param identifier
     *            the identifer
     * @param the
     *            properties for the configuration of the source
     * @return
     * @throws ConfigurationException
     */
    public Source createSource(Object identifier) throws ConfigurationException {
        return createSource(identifier, System.getProperties());
    }

}
