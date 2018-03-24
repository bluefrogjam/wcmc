package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.statistic.replacement;

import java.util.List;
import java.util.Map;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api.Scan;
import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.ContentObject;

public class DummyReplacementWhichDoesNothing extends ReplaceWithQuantIntesnityBasedOnAverageRTwithRiCurveFallback{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void replaceAction(List<Scan> scans, int quantmass,
								 ContentObject<Double> object, List<Scan> cache, double timeOfOrigin, Map<Scan,Double> retentionTimeCache) {


		//doing nothing...
		getLogger().warn("dummy replacement, it won't do a thing!");
	}

	@Override
	public String getDescription() {
		return "This replacement does actually nothing and should be used, if no replacement is required";
	}

}
