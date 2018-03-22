/*
 * Created on Nov 10, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.io.dest;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * defines a destination for writing data to a stream
 * @author wohlgemuth
 * @version Nov 10, 2005
 *
 */
public interface Destination {
    
    /**
     * returns the stream belonging to this destination
     * @author wohlgemuth
     * @version Nov 10, 2005
     * @param stream
     * @return
     * @throws IOException 
     */
    public OutputStream getOutputStream() throws IOException;
    
    /**
     * sets the identifier of this destination
     * @author wohlgemuth
     * @version Nov 10, 2005
     * @param o
     * @throws ConfigurationException 
     */
    public void setIdentifier(Object o) throws ConfigurationException;
    
    /**
     * configure the destination
     * @author wohlgemuth
     * @version Nov 10, 2005
     * @param p
     * @throws ConfigurationException 
     */
    public void configure(Map<?, ?> p) throws ConfigurationException;
}
