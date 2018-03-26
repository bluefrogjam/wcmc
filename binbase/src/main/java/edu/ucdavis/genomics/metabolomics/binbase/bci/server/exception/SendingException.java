/**
 * 
 */
package edu.ucdavis.genomics.metabolomics.binbase.bci.server.exception;

/**
 * @author wohlgemuth
 *
 */
public class SendingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * 
	 */
	public SendingException() {
		super();
		
	}

	/**
	 * @param message
	 */
	public SendingException(String message) {
		super(message);
		
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SendingException(String message, Throwable cause) {
		super(message, cause);
		
	}

	/**
	 * @param cause
	 */
	public SendingException(Throwable cause) {
		super(cause);
		
	}

}
