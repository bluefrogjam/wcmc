package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic;

import edu.ucdavis.genomics.metabolomics.util.Describealbe;

/**
 * provides a simple description
 * @author wohlgemuth
 *
 */
public interface Describeable extends Describealbe {

	/**
	 * returns the description of this element
	 * @return
	 */
	public String getDescription();
}
