/*
 * Created on Jun 27, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.statistics.data;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * abstract class for combining columns
 *
 * @author wohlgemuth
 * @version Jun 27, 2006
 */
public abstract class ColumnCombiner {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Element config;

    /**
     * combins all data in the list into a single object
     *
     * @param data
     * @return
     * @throws ConfigurationException
     * @throws ConfigurationException
     * @author wohlgemuth
     * @version Jun 27, 2006
     */
    public final Object combine(List data) throws RuntimeException {
        if (this.isConfigNeeded()) {
            logger.debug("configure this combiner");
            configure(this.getConfig());
        }

        Object result = doWork(data);
        //logger.debug("combine content: " + data + " ===> " + result);
        return result;
    }

    /**
     * does the actual work
     *
     * @param data
     * @return
     * @author wohlgemuth
     * @version Nov 2, 2006
     */
    protected abstract Object doWork(List data);

    /**
     * does the actual configuration
     *
     * @param e
     * @author wohlgemuth
     * @version Nov 2, 2006
     */
    protected void configure(Element e) throws RuntimeException {

    }

    /**
     * sets a configuration for the combiner if suported
     */
    public void setConfig(Element element) {
        this.config = element;
    }

    /**
     * does this combiner needs a config
     *
     * @return
     * @author wohlgemuth
     * @version Nov 2, 2006
     */
    public boolean isConfigNeeded() {
        return false;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Element getConfig() {
        return config;
    }
}
