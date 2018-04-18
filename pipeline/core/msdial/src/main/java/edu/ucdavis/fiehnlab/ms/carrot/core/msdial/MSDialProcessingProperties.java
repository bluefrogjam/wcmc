package edu.ucdavis.fiehnlab.ms.carrot.core.msdial;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.IonMode;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.AccuracyType;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MSDataType;

/**
 * Created by diego on 10/13/2016.
 */
public abstract class MSDialProcessingProperties {

    /**
     * omit masses smaller than this
     */
    public double massRangeBegin;

    /**
     * skipp masses bigger than this
     */
    public double massRangeEnd;

    /**
     * start of the run in minutes
     */
    public double retentionTimeBegin;

    /**
     * end of the run in minutes
     */
    public double retentionTimeEnd;

    /**
     * smoothing algorithm
     * possible values:
     * "LINEAR_WEIGHTED_MOVING_AVERAGE" (default),
     * <p>
     * not implemented yet
     * "savitzky-golay",
     * "SIMPLE_MOVING_AVERAGE",
     * "LOESS",
     * "LOWESS",
     * "BINOMIAL"
     */
    public String smoothingMethod = "LINEAR_WEIGHTED_MOVING_AVERAGE";

    /**
     * number of smoothing iterations
     */
    public int smoothingLevel;

    /**
     * # of scans in average a peak has
     */
    public int averagePeakWidth;

    /**
     * minimum intensity to consider a peak as such
     */
    public double minimumAmplitude;

    /**
     * accuracy type, accurate or nominal mass
     */
    public AccuracyType accuracyType;

    /**
     * size of the step when analyzing the mass scale
     */
    public double massSliceWidth;

    /**
     * difference between 2 masses to be considered the same
     */
    public double massAccuracy;

    /**
     * minimum scans to consider a peak as such
     */
    public double minimumDataPoints;

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
     */
    public MSDataType dataType;

    /**
     * ionMode
     */
    public IonMode ionMode;

    /**
     * Tolerance for MS1 centroiding
     */
    public double centroidMS1Tolerance;

    /**
     * Tolerance for MS2 centroiding
     */
    public double centroidMS2Tolerance;

    /**
     * Maximum charge number to search for in isotopic detection
     */
    public int maxChargeNumber;

    /**
     * Maximum isotope to trace (i.e., M + 8)
     */
    public int maxTraceNumber;

    /**
     * Sets the type of centroiding performed during deconvolution, true indicating
     * peak detection based and false sweep bin based
     */
    public boolean peakDetectionBasedCentroid;

    /**
     *
     */
    public boolean removeAfterPrecursor;

    /**
     *
     */
    public double keptIsotopeRange;

    /**
     * minimum fragment intensity for deconvolution
     */
    public double amplitudeCutoff;

    /**
     * constant to calculate the optimal shape of a gaussian peak
     *
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
                "\nionMode: " + ionMode +
                "\namplitudeCutoff: " + amplitudeCutoff +
                "\nminimumDataPoints: " + minimumDataPoints
        );
    }
}
