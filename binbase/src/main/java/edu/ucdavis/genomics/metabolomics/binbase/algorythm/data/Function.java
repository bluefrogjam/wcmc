package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data;

import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.BinObject;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.SampleObject;

/**
 * possible to apply a funtion based on some data
 * @author wohlgemuth
 *
 */
public interface Function {

	/**
	 * does a transformation on the give object based on the other data
	 * @param object
	 * @param bin
	 * @param sample
	 * @return
	 * @throws BinBaseException 
	 * @throws NumberFormatException 
	 */
	public Object apply(Object object, BinObject<String> bin, SampleObject<String> sample) throws NumberFormatException, BinBaseException;
}
