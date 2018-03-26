/*
 * Created on Nov 9, 2005
 */
package edu.ucdavis.genomics.metabolomics.exception;

/**
 * is thrown if we can't obtain a factory
 * @author wohlgemuth
 * @version Nov 9, 2005
 *
 */
public class FactoryException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 2L;

    public FactoryException() {
        super();
    }

    public FactoryException(String arg0) {
        super(arg0);
    }

    public FactoryException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public FactoryException(Throwable arg0) {
        super(arg0);
    }
}
