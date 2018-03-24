package edu.ucdavis.genomics.metabolomics.binbase.algorythm.filter;

import java.util.Map;

/**
 * does absolutely nothing
 * @author wohlgemuth
 *
 */
public class DoNothingModifier extends MassSpecModifier{

	public DoNothingModifier() {
	}

	@Override
	public Map<String, Object> modify(Map<String, Object> spectra) {
		logger.info("do nothing!!!");
		return spectra;
	}

}
