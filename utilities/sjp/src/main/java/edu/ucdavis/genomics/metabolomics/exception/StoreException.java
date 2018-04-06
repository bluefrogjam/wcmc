/*
 * Created on 02.06.2004
 */
package edu.ucdavis.genomics.metabolomics.exception;


/**
 * @author wohlgemuth
 */
public class StoreException extends DataStoreException {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2L;

    /**
     *
     */
    public StoreException() {
        super();
    }

    /**
     * @param message
     */
    public StoreException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public StoreException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public StoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
