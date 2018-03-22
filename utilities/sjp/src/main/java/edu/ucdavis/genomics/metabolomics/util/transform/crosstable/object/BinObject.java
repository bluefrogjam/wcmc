/*
 * Created on Jun 16, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object;

import org.xml.sax.Attributes;

public class BinObject  <Type>extends MetaObject<Type>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	public BinObject(Type value) {
		super(value);
	}
	public BinObject(Type value, Attributes a) {
		super(value,a);
	}
}
