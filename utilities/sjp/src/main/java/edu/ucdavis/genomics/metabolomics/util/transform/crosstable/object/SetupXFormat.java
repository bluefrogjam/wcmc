package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object;

import org.xml.sax.Attributes;

public class SetupXFormat<Type> extends HeaderFormat<Type> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SetupXFormat(Type value, Attributes a) {
		super(value, a);
	}

	public SetupXFormat(Type value) {
		super(value);
	}
}
