package edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object;

import org.xml.sax.Attributes;

public class CalculationObject<Type> extends MetaObject<Type> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	private String hyperlink;

	public CalculationObject(Type value) {
		super(value);
	}

	public CalculationObject(Type value, String hyperlink) {
		super(value);
		this.setHyperlink(hyperlink);
	}

	public CalculationObject(Type value, Attributes a, String hyperlink) {
		super(value, a);
		this.setHyperlink(hyperlink);
	}

	public String getHyperlink() {
		return hyperlink;
	}

	public void setHyperlink(String hyperlink) {
		this.hyperlink = hyperlink;
	}

}
