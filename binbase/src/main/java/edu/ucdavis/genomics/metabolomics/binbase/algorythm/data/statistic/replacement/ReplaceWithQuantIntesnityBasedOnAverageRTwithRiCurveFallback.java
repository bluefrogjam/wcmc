package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.replacement;

import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;

/**
 * replaces the zero values with values found by the average rt time of the day
 * of if no annotations are found for this date we will apply a retention index
 * curve based on the standards found in this sample
 * 
 * @author wohlgemuth
 */
public class ReplaceWithQuantIntesnityBasedOnAverageRTwithRiCurveFallback extends ReplaceWithQuantIntensityBasedOnRICurve {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	public String getFolder() {
		return "averageTimeWithRIFallback";
	}

	//MULTI THREAD
	@Override
	protected double getRetentionTimeForBin(ContentObject<Double> object) throws NumberFormatException, BinBaseException {
		
		getLogger().debug("obtaining retention time for bin: "+ object);
		Double averageRT = this.getFile().getAverageRetentionTimeForBin(Integer.parseInt(object.getAttributes().get("id").toString()),
				getFile().getSample(Integer.parseInt(object.getAttributes().get("sample_id"))).getValue());

		//averageRT = Double.NEGATIVE_INFINITY;
		getLogger().debug("calculated time was: " + averageRT);
		if (averageRT == null || Double.isInfinite(averageRT)) {
			getLogger().debug("no average rt found for this bin! falling back");
			return super.getRetentionTimeForBin(object);
		}
		else {
			getLogger().debug("using average rt by day: " + averageRT);
			return averageRT;
		}
	}

	/**
	 * preferred method in case of failure is to use the retention index curve
	 */
	@Override
	protected double getPrefferendRetentionTimeForBin(
			ContentObject<Double> object) throws NumberFormatException,
			BinBaseException {
		getLogger().debug("using preferred time...");
		return getRetentionTimeBasedOnCorrectionCurve(object);
	}
	
	

}
