package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.replacement;

import java.io.File;
import java.io.Serializable;

import edu.ucdavis.genomics.metabolomics.binbase.bci.server.exception.FileNotFoundException;

/**
 * finds fils for us
 * @author wohlgemuth
 *
 */
public interface RawdataResolver extends Serializable, Comparable<RawdataResolver> {

	/**
	 * gets us the file for the specific name
	 * @param sampleName
	 * @return
	 */
	public File resolveNetcdfFile(String sampleName) throws FileNotFoundException;
	
	/**
	 * returns the priority
	 * @return
	 */
	public Integer getPriority();
}
