/*
 * Created on Dec 6, 2005
 */
package edu.ucdavis.genomics.metabolomics.exception;

public class LockingException extends BinBaseException {

    /**
     *
     */
    private static final long serialVersionUID = 2L;

    public LockingException(String arg0, Throwable arg1) {
        super(arg0, arg1);

    }

    public LockingException(String arg0) {
        super(arg0);

    }

    public LockingException(Throwable arg0) {
        super(arg0);

    }

    public LockingException() {
        super();

    }

}
