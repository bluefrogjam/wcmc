/*
 * Created on Sep 11, 2006
 */
package edu.ucdavis.genomics.metabolomics.binbase.bci.server.exception;

import java.util.Collection;

import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;

/**
 * @author wohlgemuth
 * @version Sep 11, 2006
 *
 * @
 */
public class FileNotFoundException extends BinBaseException{
	
	private static final long serialVersionUID = 1L;

	/**
	 * collection of exceptions
	 */
	private Collection exceptions;
	
	/**
	 * constructur using an collection to store the data
	 * @author wohlgemuth
	 * @version Sep 11, 2006
	 * @param exceptions
	 */
	public FileNotFoundException(Collection exceptions){
		this.exceptions = exceptions;
	}
	
	public FileNotFoundException() {
		super();
	}
	
	public FileNotFoundException(String arg0) {
		super(arg0);
	}
	
	public FileNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
	
	public FileNotFoundException(Throwable arg0) {
		super(arg0);
	}
	
	public String toString() {
		if(exceptions != null){
		return super.toString() + " - " + exceptions;
		}
		return super.toString();
	}
	
	public Collection getExceptions(){
		return exceptions;
	}
}
