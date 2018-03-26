/*
 * Created on Nov 17, 2005
 */
package edu.ucdavis.genomics.metabolomics.exception;

/**
 * @author wohlgemuth
 * @version Nov 17, 2005
 *
 */
public class ValidationException extends BinBaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * @author wohlgemuth
	 * @version Nov 17, 2005
	 */
	public ValidationException() {
		super();
		
	}

	/**
	 * @author wohlgemuth
	 * @version Nov 17, 2005
	 * @param arg0
	 */
	public ValidationException(String arg0) {
		super(arg0);
		
	}

	/**
	 * @author wohlgemuth
	 * @version Nov 17, 2005
	 * @param arg0
	 * @param arg1
	 */
	public ValidationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		
	}

	/**
	 * @author wohlgemuth
	 * @version Nov 17, 2005
	 * @param arg0
	 */
	public ValidationException(Throwable arg0) {
		super(arg0);
		
	}

}
