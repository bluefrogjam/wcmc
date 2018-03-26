package edu.ucdavis.genomics.metabolomics.binbase.algorythm.matching.anotation;

import java.util.Map;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate.ValidateSpectra;
import edu.ucdavis.genomics.metabolomics.util.config.Configable;

/**
 * filters by unique mass, basically if the unique mass is large enough
 * 
 * @author wohlgemuth
 * 
 */
public class UniqueRatioFilter extends BasicFilter {

	private double minRatio;

	/**
	 * constructor and configures this filter
	 */
	public UniqueRatioFilter() {
		try {
			minRatio = Configable.CONFIG.getElement("values.filter.unique")
					.getAttribute("ratio").getDoubleValue();
			if(logger.isDebugEnabled()) {
				logger.debug("set min ratio to: " + minRatio);
			}
		} catch (Exception e) {
			logger.error("error at getting value, using default value. Exception was: "
					+ e.getMessage(), e);
			minRatio = 0.05;
		}
	}

	/**
	 * returns true if the unique mass is large enough and the peak is not to
	 * large
	 */
	protected boolean compare(Map<String, Object> bin,
			Map<String, Object> unknown) {
		int binId = Integer.parseInt(bin.get("bin_id").toString());
		int spectraId = Integer.parseInt(unknown.get("spectra_id").toString());
		if (FilterUtilities.isLargePeak(FilterUtilities
				.getSingnalNoise(unknown)) == true) {
			logger.debug("found a large peak --> ignore unique ions");
			getDiagnosticsService()
					.diagnosticActionSuccess(
							spectraId,
							binId,
							this.getClass(),
							"filtering by unique ion ratio",
							"massspec was accepted, since it's a very large peak and for this reason we disable this filter",
							new Object[] {});
			return true;
		} else {

			double[][] unkSpectra = FilterUtilities.calculateMassSpec(unknown);

			int basePeak = FilterUtilities.calculateBasePeak(unkSpectra);
			double baseIntensity = unkSpectra[basePeak - 1][ValidateSpectra.FRAGMENT_ABS_POSITION];

			int unkUnique = FilterUtilities.getUniqueMass(unknown);
			int unique = FilterUtilities.getUniqueMass(bin);

			double uniqueIntensity = unkSpectra[unique - 1][ValidateSpectra.FRAGMENT_ABS_POSITION];

			if (isDebugEnabled()) {

				logger.debug("unique intensity: " + uniqueIntensity);
				logger.debug("base intensity: " + baseIntensity);
				logger.debug("value: " + (uniqueIntensity / baseIntensity));
				logger.debug("needed value: " + minRatio);
			}
			if ((uniqueIntensity / baseIntensity) > minRatio) {
				getDiagnosticsService()
						.diagnosticActionSuccess(
								spectraId,
								binId,
								this.getClass(),
								"filtering by unique ion ratio",
								"massspec was accepted, since the ratio between the unique intensity and the baseintensity was higher than the minimu ratio",
								new Object[] {
										(uniqueIntensity / baseIntensity),
										minRatio });
				return true;
			} else {
				this.setReasonForRejection("unique intensity divided by base peak intensity was not higher than "
						+ minRatio);
				getDiagnosticsService()
						.diagnosticActionFailed(
								spectraId,
								binId,
								this.getClass(),
								"filtering by unique ion ratio",
								"massspec was rejected, since the ratio between the unique intensity and the baseintensity was smaller than the minimum ratio",
								new Object[] {
										(uniqueIntensity / baseIntensity),
										minRatio });
				return false;
			}

		}
	}

}
