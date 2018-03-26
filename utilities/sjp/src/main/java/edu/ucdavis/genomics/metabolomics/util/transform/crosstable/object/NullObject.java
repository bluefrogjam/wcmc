/*
 * Created on Jun 16, 2006
 */
package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

public class NullObject <Type>extends ContentObject<Type>{

	private static final long serialVersionUID = 2L;

	public NullObject(Type value) {
		super(value);
	}
	
	public NullObject(Type value, Attributes a) {
		super(value,a);
	}

	public NullObject(Type m, Map<String,String> attributes) {
		super(m,attributes);
	}

}
