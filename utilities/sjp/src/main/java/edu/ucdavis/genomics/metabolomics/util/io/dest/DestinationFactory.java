/*
 * Created on Nov 9, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.io.dest;

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
public abstract class DestinationFactory extends AbstractFactory{

    /**
     * name of the property
     */
    public static final String DEFAULT_PROPERTY_NAME = DestinationFactory.class.getName();

    private static String foundFactory = null;

    public DestinationFactory() {
        super();
    }

    /**
     * returns an new instance of the factory
     * 
     * @author wohlgemuth
     * @version Nov 9, 2005
     * @return
     */
    public static DestinationFactory newInstance() {

        // Locate Factory
        foundFactory = findFactory(
                DEFAULT_PROPERTY_NAME,
                FileDestinationFactoryImpl.class.getName());
        
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
    @SuppressWarnings("unchecked")
	public static DestinationFactory newInstance(String factoryClass) {
        Class classObject;
        DestinationFactory factory;

        try {
            classObject = Class.forName(factoryClass);
            factory = (DestinationFactory) classObject.newInstance();
            return factory;

        } catch (Exception e) {
            throw new FactoryException(e);
        }
    }

    
    /**
     * creates a new destination
     * 
     * @author wohlgemuth
     * @version Nov 9, 2005
     * @param identifier
     *            the identifer
     * @param the
     *            properties for the configuration of the destination
     * @return
     * @throws ConfigurationException
     */
    public abstract Destination createDestination(Object identifier, Map<?, ?> propertys)
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
    public Destination createDestination(Object identifier) throws ConfigurationException {
        return createDestination(identifier, System.getProperties());
    }
}
