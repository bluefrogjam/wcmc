/*
 * Created on 24.06.2005
 */
package edu.ucdavis.genomics.metabolomics.exception;


/**
 * @author wohlgemuth
 */
public class ConvertException extends BinBaseException {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2L;

    /**
     *
     */
    public ConvertException() {
        super();


    }

    /**
     * @param message
     */
    public ConvertException(String message) {
        super(message);


    }

    /**
     * @param message
     * @param cause
     */
    public ConvertException(String message, Throwable cause) {
        super(message, cause);


    }

    /**
     * @param cause
     */
    public ConvertException(Throwable cause) {
        super(cause);


    }
}
