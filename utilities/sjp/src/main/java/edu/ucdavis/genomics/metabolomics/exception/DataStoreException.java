/*
 * Created on 02.06.2004
 */
package edu.ucdavis.genomics.metabolomics.exception;


/**
 * @author wohlgemuth
 */
public class DataStoreException extends BinBaseException {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2L;

    /**
     * @param message
     */
    public DataStoreException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public DataStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public DataStoreException(Throwable cause) {
        super(cause);
    }

    /**
     *
     */
    public DataStoreException() {
        super();
    }
}
