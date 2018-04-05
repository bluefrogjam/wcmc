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
 *
 * @author wohlgemuth
 * @version Nov 10, 2005
 */
public interface Destination {

    /**
     * returns the stream belonging to this destination
     *
     * @param stream
     * @return
     * @throws IOException
     * @author wohlgemuth
     * @version Nov 10, 2005
     */
    public OutputStream getOutputStream() throws IOException;

    /**
     * sets the identifier of this destination
     *
     * @param o
     * @throws ConfigurationException
     * @author wohlgemuth
     * @version Nov 10, 2005
     */
    public void setIdentifier(Object o) throws ConfigurationException;

    /**
     * configure the destination
     *
     * @param p
     * @throws ConfigurationException
     * @author wohlgemuth
     * @version Nov 10, 2005
     */
    public void configure(Map<?, ?> p) throws ConfigurationException;
}
