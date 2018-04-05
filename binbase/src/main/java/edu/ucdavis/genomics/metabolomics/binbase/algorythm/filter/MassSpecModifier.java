package edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter;

import java.util.List;
import java.util.Map;

import org.jdom.Element;

import edu.ucdavis.genomics.metabolomics.util.SQLObject;

/**
 * a simple interface to allow modification of masss specs, but won't write changes to the database
 * @author wohlgemuth
 *
 */
public abstract class MassSpecModifier extends SQLObject{

	/**
	 * contains the configuration for this mass spec modifier.
	 */
	protected Element configuration;

	/**
	 * modifies the given map and returns the new map.
	 * @param spectra
	 * @return
	 */
	public abstract Map<String, Object> modify(Map<String, Object> spectra);
	
	public final void setConfiguration(Element e){
		this.configuration = e;
		if(e.getChildren() != null){
			doConfigure(e.getChildren());
		}
	}

	/**
	 * configures this specific modified with all these elements
	 * @param e
	 */
	protected void doConfigure(List<Element> e){
		
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
