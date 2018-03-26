/*
 * Created on 02.06.2004
 */
package edu.ucdavis.genomics.metabolomics.exception;


/**
 * @author wohlgemuth
 */
public class NotSupportedException extends RuntimeException {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2L;

    /**
     *
     */
    public NotSupportedException() {
        super();
    }

    /**
     * @param message
     */
    public NotSupportedException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public NotSupportedException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public NotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
