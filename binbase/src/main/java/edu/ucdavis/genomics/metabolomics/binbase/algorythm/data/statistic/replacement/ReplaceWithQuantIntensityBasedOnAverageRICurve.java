package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.replacement;

import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.HeaderFormat;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.SampleObject;

/**
 * replaces the values with the average retention index for a given day
 * @author wohlgemuth
 *
 */
public class ReplaceWithQuantIntensityBasedOnAverageRICurve extends ReplaceWithQuantIntensityBasedOnRICurve{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * returns the average retention index for a given bin for a given day
	 * @param bin
	 * @param sample
	 * @return
	 * @throws BinBaseException 
	 * @throws NumberFormatException 
	 */
	protected double getRetentionIndexForBin(HeaderFormat<String> bin,SampleObject<String> sample) throws NumberFormatException, BinBaseException{
		return getFile().getAverageRetentionIndexForBin(Integer.parseInt(bin.getAttributes().get("id").toString()),sample.getValue());
	}
	
	@Override
	public String getFolder() {
		return "averageRiCurve";
	}
}
