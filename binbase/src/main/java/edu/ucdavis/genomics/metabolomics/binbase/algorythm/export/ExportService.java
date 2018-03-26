/*
 * Created on Nov 18, 2005
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.export;

import java.util.Properties;

/**
 * provides as with an export service
 * @author wohlgemuth
 * @version Nov 18, 2005
 *
 */
public interface ExportService{
	/**
	 * exports the given experiment
	 * @author wohlgemuth
	 * @version Nov 18, 2005
	 * @param exp
	 * @return the generated id for this result
	 */
	public int export(Experiment exp);
	
	/**
	 * sets specific properties
	 * @author wohlgemuth
	 * @version Jan 20, 2006
	 * @param p
	 */
	public void setProperties(Properties p);
}
