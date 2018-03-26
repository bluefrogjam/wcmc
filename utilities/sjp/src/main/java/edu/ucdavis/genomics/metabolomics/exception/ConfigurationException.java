/*
 * Created on Jun 10, 2005
 */
package edu.ucdavis.genomics.metabolomics.exception;


/**
 * should be used to say something with the configuration went wrong
 * @author wohlgemuth
 *
 */
public class ConfigurationException extends BinBaseException {
    /**
         * Comment for <code>serialVersionUID</code>
         */
    private static final long serialVersionUID = 2L;

    /**
    *
    */
    public ConfigurationException() {
        super();

        
    }

    /**
     * @param arg0
     */
    public ConfigurationException(String arg0) {
        super(arg0);

        
    }

    /**
     * @param arg0
     * @param arg1
     */
    public ConfigurationException(String arg0, Throwable arg1) {
        super(arg0, arg1);

        
    }

    /**
     * @param arg0
     */
    public ConfigurationException(Throwable arg0) {
        super(arg0);

        
    }
}
