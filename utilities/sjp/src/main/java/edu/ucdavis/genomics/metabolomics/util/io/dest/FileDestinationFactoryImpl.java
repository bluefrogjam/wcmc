/*
 * Created on Nov 9, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.io.dest;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;

import java.util.Map;

/**
 * used to create destination based on files and configure these destinations
 * @author wohlgemuth
 * @version Nov 9, 2005
 *
 */
public class FileDestinationFactoryImpl extends DestinationFactory{

    public FileDestinationFactoryImpl() {
        super();
    }

    /**
     * creates a new destionation, file based
     * @author wohlgemuth
     * @version Nov 10, 2005
     * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.dest.DestinationFactory#createDestination(Object, Map)
     */
    public FileDestination createDestination(Object identifier, Map<?,?> propertys) throws ConfigurationException {
    	FileDestination destination =new FileDestination();
        destination.configure(propertys);
        destination.setIdentifier(identifier);
        return destination;
    }
}
