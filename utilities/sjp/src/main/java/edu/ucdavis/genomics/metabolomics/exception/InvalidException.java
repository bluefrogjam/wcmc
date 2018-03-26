/*
 * Created on 05.02.2004
 */
package edu.ucdavis.genomics.metabolomics.exception;


/**
 * @author wohlgemuth
 */
public class InvalidException extends BinBaseException {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2L;

    /**
     *
     */
    public InvalidException() {
        super();
    }

    /**
     * @param arg0
     */
    public InvalidException(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     */
    public InvalidException(Throwable arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public InvalidException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
