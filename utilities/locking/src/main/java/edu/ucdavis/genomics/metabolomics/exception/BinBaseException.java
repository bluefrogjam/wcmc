/*
 * Created on Dec 6, 2005
 */
package edu.ucdavis.genomics.metabolomics.exception;

/**
 * standard exception for binbase related problems
 *
 * @author wohlgemuth
 * @version Dec 6, 2005
 */
public class BinBaseException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 2L;

    public BinBaseException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public BinBaseException(String arg0) {
        super(arg0);
    }

    public BinBaseException(Throwable arg0) {
        super(arg0);
    }

    public BinBaseException() {
        super();
    }

}
