package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.replacement;

import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;

/**
 * calculates the retention times for the bin over all samples of the same day
 * of the given sample
 * 
 * @author wohlgemuth
 * 
 */
public class ReplaceWithQuantIntensityBasedOnAverageBinRtOverADay extends OldReplaceWithQuantIntensity3 {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected double getRetentionTimeForBin(ContentObject<Double> object) throws NumberFormatException, BinBaseException {
		return this.getFile().getAverageRetentionTimeForBin(Integer.parseInt(object.getAttributes().get("id").toString()),
				getFile().getSample(Integer.parseInt(object.getAttributes().get("sample_id"))).getValue());
	}
	
	public String getFolder() {
		return "averageRtOverDay";
	}

}
