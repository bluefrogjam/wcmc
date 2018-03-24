package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.replacement;

import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;

/**
 * calculates retention times for bins over the complete experiement
 * @author wohlgemuth
 *
 */
public class ReplaceWithQuantIntensityBasedOnAverageBinRtOverTheExperiment extends OldReplaceWithQuantIntensity3{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected double getRetentionTimeForBin(ContentObject<Double> object){
		return this.getFile().getAverageRetentionTimeForBin(Integer.parseInt(object.getAttributes().get("id").toString()));	
	}
	
	public String getFolder() {
		return "averageRtOverExperiment";
	}

}
