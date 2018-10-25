/*
 * Created on May 14, 2005
 */
package edu.ucdavis.genomics.metabolomics.exception;

import java.rmi.RemoteException;


/**
 * @author wohlgemuth
 */
public class AlreadyRegisteredException extends RemoteException {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2L;

    /**
     *
     */
    public AlreadyRegisteredException() {
        super();
    }

    /**
     * @param arg0
     */
    public AlreadyRegisteredException(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public AlreadyRegisteredException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
