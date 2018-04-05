/*
 * Created on 02.06.2004
 */
package edu.ucdavis.genomics.metabolomics.exception;


/**
 * @author wohlgemuth
 */
public class TransactionException extends DataStoreException {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2L;

    /**
     *
     */
    public TransactionException() {
        super();
    }

    /**
     * @param message
     */
    public TransactionException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public TransactionException(Throwable cause) {
        super(cause);
    }
}
