/*
 * Created on Jun 30, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object;

import java.util.Map;

import org.xml.sax.Attributes;

public class CombinedObject<Type> extends ContentObject<Type> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	public CombinedObject(Type value, Map<String,String> attributes) {
		super(value, attributes);
		
	}

	public CombinedObject(Type value) {
		super(value);
	}

	public CombinedObject(Type value, Attributes a) {
		super(value, a);
	}
}
