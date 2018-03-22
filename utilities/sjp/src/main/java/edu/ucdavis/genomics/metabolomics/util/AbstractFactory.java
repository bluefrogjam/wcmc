/*
 * Created on Dec 6, 2005
 */
package edu.ucdavis.genomics.metabolomics.util;

import edu.ucdavis.genomics.metabolomics.exception.FactoryException;
import edu.ucdavis.genomics.metabolomics.util.config.XMLConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * 
 * @author wohlgemuth
 * @version Dec 6, 2005
 *
 */
public abstract class AbstractFactory{
	/**
	 * needed for our logging
	 */
	protected static Logger logger = LoggerFactory.getLogger(AbstractFactory.class);
	
	public AbstractFactory() {
		super();
	}

    /**
     * finds our factory
     * 
     * @author wohlgemuth
     * @version Nov 9, 2005
     * @param property
     * @param defaultValue
     * @return
     */
    protected final static String findFactory(String property, String defaultValue) {
        String factory;

        // Check System Property
        factory = System.getProperty(property);
        if (factory == null) {
            // Check Configurator
            try {
                Properties p = XMLConfigurator.getInstance().getProperties();
                factory = p.getProperty(property);
            } catch (Exception e) {
            }
        }

        if (factory == null) {
            factory = defaultValue;
        }

        if (factory == null) {
        	throw new FactoryException("no default implementation available");
        }
        
        logger.debug("using factory: " + factory); 
        return factory;
    }      

}
