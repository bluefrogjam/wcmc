/*
 * Created on Nov 9, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.io.source;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;

import java.util.Map;

/**
 * used to create sources based on files and configure these sources
 * @author wohlgemuth
 * @version Nov 9, 2005
 *
 */
public class FileSourceFactoryImpl extends SourceFactory{

    public FileSourceFactoryImpl() {
        super();
    }

    /**
     * returns a new filesource
     * @author wohlgemuth
     * @version Nov 9, 2005
     * @throws ConfigurationException 
     * @see edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.data.source.SourceFactory#createSource()
     */
    public FileSource createSource(Object identifier, Map<?, ?> propertys) throws ConfigurationException {
    	FileSource source =new FileSource();
        source.configure(propertys);
        source.setIdentifier(identifier);
        return source;
    }
}
