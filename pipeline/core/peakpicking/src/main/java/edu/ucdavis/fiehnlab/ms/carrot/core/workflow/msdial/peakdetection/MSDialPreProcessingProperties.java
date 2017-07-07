package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by diego on 10/13/2016.
 */
@Component
@ConfigurationProperties(prefix = "peakdetection")
public class MSDialPreProcessingProperties {
	/**
	 * omit masses smaller than this
	 */
	public double massRangeBegin = 100;

	/**
	 * skipp masses bigger than this
	 */
	public double massRangeEnd = 1700;

	/**
	 * start of the run
	 */
	public double retentionTimeBegin = 0.5;

	/**
	 * end of the run
	 */
	public double retentionTimeEnd = 12.5;

	/**
	 * smoothing algorythm
	 * possible values:
	 *  "LINEAR_WEIGHTED_MOVING_AVERAGE" (default),
	 *
	 *  not implemented yet
	 *  "savitzky-golay",
	 *  "SIMPLE_MOVING_AVERAGE",
	 *  "LOESS",
	 *  "LOWESS",
	 *  "BINOMIAL"
	 */
	public String smoothingMethod = "LINEAR_WEIGHTED_MOVING_AVERAGE";

	/**
	 * number of smoothing iterations
	 */
	public int smoothingLevel = 1;

	/**
	 * # of scans in average a peak has
	 */
	public int averagePeakWidth = 10;

	/**
	 * minimum intensity to consider a peak as such
	 */
	public double minimumAmplitude = 1000;

	/**
	 * Accurate or Nominal mass
	 * possible values: "accurate" or "nominal"
	 */
	public String massResolution = "accurate";

	/**
	 * size of the step when analyzing the mass scale
	 */
	public double massSliceWidth = 0.1;

	/**
	 * difference between 2 masses to be considered the same
	 */
	public double massAccuracy = 0.05;

	/**
	 * minimum scans to consider a peak as such
	 */
	public double minimumDataPoints = 5;

	/**
	 * multiplier for calculation of noise at base line
	 * do not change
	 */
	public double amplitudeNoiseFactor = 4;

	/**
	 * multiplier for calculation of noise at peak edges
	 * do not change
	 */
	public double slopeNoiseFactor = 2;

	/**
	 * multiplier for peak top calculation of noise
	 * do not change
	 */
	public double peaktopNoiseFactor = 2;

	/**
	 * not really any idea of what it means
	 * its always true in MS-Dial
	 */
	public boolean backgroundSubtraction = true;

	/**
	 * Centroided or Profiled data
	 * possible values: "centroid" or "profile"
	 */
	public String dataType = "centroid";

	/**
	 * ionMode
	 * possible valued: '+' or '-'
	 */
	public char ionMode = '+';

	/**
	 * minimum fragment intensity for centroiding
	 */
	public double amplitudeCutoff = 1;

	/**
	 * constant to calculate the optimal shape of a gaussian peak
	 * @return
	 */
	public double sigma = 0.1;

	public String toString() {
		return (
				"massRangeBegin: " + massRangeBegin +
				"\nmassRangeEnd: " + massRangeEnd +
				"\nretentionTimeBegin: " + retentionTimeBegin +
				"\nretentionTimeEnd: " + retentionTimeEnd +
				"\nsmoothingMethod: " + smoothingMethod +
				"\nsmoothingLevel: " + smoothingLevel +
				"\naveragePeakWidth: " + averagePeakWidth +
				"\nminimumAmplitude: " + minimumAmplitude +
				"\nmassSliceWith: " + massSliceWidth +
				"\nmassAccuracy: " + massAccuracy +
				"\ndataType: " + dataType +
				"\nionMode: " + ionMode +
				"\namplitudeCutoff: " + amplitudeCutoff +
				"\nminimumDataPoints: " + minimumDataPoints
		);
	}
}
