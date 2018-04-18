package edu.ucdavis.fiehnlab.ms.carrot.core.msdial;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.IonMode;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MSDataType;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.AccuracyType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by diego on 10/13/2016.
 */
@ConfigurationProperties
@Component
public class MSDialProcessingProperties {

    /**
     * omit masses smaller than this
     */
    public double massRangeBegin = 100;

    /**
     * skipp masses bigger than this
     */
    public double massRangeEnd = 1700;

    /**
     * start of the run in minutes
     */
    public double retentionTimeBegin = 0.5;

    /**
     * end of the run in minutes
     */
    public double retentionTimeEnd = 12.5;

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
    public int smoothingLevel = 1;

    /**
     * # of scans in average a peak has
     */
    public int averagePeakWidth = 5;

    /**
     * minimum intensity to consider a peak as such
     */
    public double minimumAmplitude = 2500;

    /**
     * accuracy type, accurate or nominal mass
     */
    public AccuracyType accuracyType = AccuracyType.ACCURATE;

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
     */
    public MSDataType dataType = MSDataType.CENTROID;

    /**
     * ionMode
     */
    public IonMode ionMode = new PositiveMode();

    /**
     * constant to calculate the optimal shape of a gaussian peak
     *
     * @return
     */
    public double sigma = 0.1;

    /**
     * Tolerance for MS1 centroiding
     */
    public double centroidMS1Tolerance = 0.01;

    /**
     * Tolerance for MS2 centroiding
     */
    public double centroidMS2Tolerance = 0.1;

    /**
     * Maximum charge number to search for in isotopic detection
     */
    public int maxChargeNumber = 2;

    /**
     * Maximum isotope to trace (i.e., M + 8)
     */
    public int maxTraceNumber = 8;

    /**
     * Sets the type of centroiding performed during deconvolution, true indicating
     * peak detection based and false sweep bin based
     */
    public boolean peakDetectionBasedCentroid = true;

    /**
     *
     */
    public boolean removeAfterPrecursor = true;

    /**
     *
     */
    public double keptIsotopeRange = 0.5;

    /**
     * minimum fragment intensity for deconvolution
     */
    public double amplitudeCutoff = 10;


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
