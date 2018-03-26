/*
 * Created on Jun 16, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object;

import org.xml.sax.Attributes;

public class ZeroObject  <Type>extends ContentObject<Type>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	public ZeroObject(Type value) {
		super(value);
	}

	public ZeroObject(Type value, Attributes a) {
		super(value,a);
	}
}
