/*
 * Created on Dec 6, 2005
 */
package edu.ucdavis.genomics.metabolomics.exception;

public class AlreadyLockedException extends LockingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	public AlreadyLockedException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		
	}

	public AlreadyLockedException(String arg0) {
		super(arg0);
		
	}

	public AlreadyLockedException(Throwable arg0) {
		super(arg0);
		
	}

	public AlreadyLockedException() {
		super();
		
	}

}
