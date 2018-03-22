package edu.ucdavis.genomics.metabolomics.sjp.finder;

import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;

public class ParserNotFoundException extends ParserException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ParserNotFoundException() {
		super();
	}

	public ParserNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ParserNotFoundException(String arg0) {
		super(arg0);
	}

	public ParserNotFoundException(Throwable arg0) {
		super(arg0);
	}

}
