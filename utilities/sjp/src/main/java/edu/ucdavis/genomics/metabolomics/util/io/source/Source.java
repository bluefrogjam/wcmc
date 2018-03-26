/*
 * Created on Nov 8, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.io.source;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * defines a datasource to read data
 * @author wohlgemuth
 * @version Nov 8, 2005
 *
 */
public interface Source {
    
    /**
     * returns an inputstream
     * @author wohlgemuth
     * @version Nov 8, 2005
     * @return
     */
    public InputStream getStream() throws IOException;
    
    /**
     * returns the name of the source
     * @author wohlgemuth
     * @version Nov 9, 2005
     * @return
     */
    public String getSourceName();
    
    /**
     * sets the identifier of this source
     * @author wohlgemuth
     * @version Nov 9, 2005
     * @param o
     * @throws ConfigurationException 
     */
    public void setIdentifier(Object o) throws ConfigurationException;
    
    /**
     * configure the source
     * @author wohlgemuth
     * @version Nov 8, 2005
     * @param p
     * @throws ConfigurationException 
     */
    public void configure(Map<?, ?> p) throws ConfigurationException;
    
    /**
     * does this source actually exist, if not we have to deal with it!
     * @author wohlgemuth
     * @version Nov 17, 2005
     * @return
     */
    public boolean exist();

    /**
     * returns the version of the source 
     * @author wohlgemuth
     * @version Mar 15, 2006
     * @return
     */
    public long getVersion();
}
