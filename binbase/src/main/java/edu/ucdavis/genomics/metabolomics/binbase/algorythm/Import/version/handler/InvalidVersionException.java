/*
 * Created on Jan 18, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.Import.version.handler;

import edu.ucdavis.genomics.metabolomics.exception.ConfigurationException;

public class InvalidVersionException extends ConfigurationException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	public InvalidVersionException() {
		super();
		
	}

	public InvalidVersionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		
	}

	public InvalidVersionException(String arg0) {
		super(arg0);
		
	}

	public InvalidVersionException(Throwable arg0) {
		super(arg0);
		
	}

}
