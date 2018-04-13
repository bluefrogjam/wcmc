/*
 * Created on Dec 6, 2005
 */
package edu.ucdavis.genomics.metabolomics.exception;

public class TimeoutException extends LockingException {

    /**
     *
     */
    private static final long serialVersionUID = 2L;

    public TimeoutException(String arg0, Throwable arg1) {
        super(arg0, arg1);

    }

    public TimeoutException(String arg0) {
        super(arg0);

    }

    public TimeoutException(Throwable arg0) {
        super(arg0);

    }

    public TimeoutException() {
        super();

    }

}
