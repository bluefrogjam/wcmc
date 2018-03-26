package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object;

import org.xml.sax.Attributes;

public class ClassFormat <Type> extends HeaderFormat<Type> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ClassFormat(Type value, Attributes a) {
		super(value, a);
	}

	public ClassFormat(Type value) {
		super(value);
	}
}
