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
 *
 * @author wohlgemuth
 * @version Nov 8, 2005
 */
public interface Source {

    /**
     * returns an inputstream
     *
     * @return
     * @author wohlgemuth
     * @version Nov 8, 2005
     */
    public InputStream getStream() throws IOException;

    /**
     * returns the name of the source
     *
     * @return
     * @author wohlgemuth
     * @version Nov 9, 2005
     */
    public String getSourceName();

    /**
     * sets the identifier of this source
     *
     * @param o
     * @throws ConfigurationException
     * @author wohlgemuth
     * @version Nov 9, 2005
     */
    public void setIdentifier(Object o) throws ConfigurationException;

    /**
     * configure the source
     *
     * @param p
     * @throws ConfigurationException
     * @author wohlgemuth
     * @version Nov 8, 2005
     */
    public void configure(Map<?, ?> p) throws ConfigurationException;

    /**
     * does this source actually exist, if not we have to deal with it!
     *
     * @return
     * @author wohlgemuth
     * @version Nov 17, 2005
     */
    public boolean exist();

    /**
     * returns the version of the source
     *
     * @return
     * @author wohlgemuth
     * @version Mar 15, 2006
     */
    public long getVersion();
}
