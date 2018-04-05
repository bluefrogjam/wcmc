/*
 * Created on 02.06.2004
 */
package edu.ucdavis.genomics.metabolomics.exception;


/**
 * @author wohlgemuth
 */
public class QueryException extends DataStoreException {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2L;

    /**
     * @param message
     */
    public QueryException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public QueryException(Throwable cause) {
        super(cause);
    }

    /**
     *
     */
    public QueryException() {
        super();
    }
}
