package edu.ucdavis.genomics.metabolomics.binbase.algorythm.exception;

public class HeaderProblemException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HeaderProblemException() {
	}

	public HeaderProblemException(String message) {
		super(message);
	}

	public HeaderProblemException(Throwable cause) {
		super(cause);
	}

	public HeaderProblemException(String message, Throwable cause) {
		super(message, cause);
	}

}
