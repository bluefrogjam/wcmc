package edu.ucdavis.fiehnlab.ms.carrot.math;

public class SimilarityMethods {

    /**
     * Gaussian similarity used for mass accuracy and retention time similarity calculations
     * @param observed
     * @param expected
     * @param tolerance
     * @return
     */
    public static double gaussianSimilarity(double observed, double expected, double tolerance) {
        return Math.exp(-0.5 * Math.pow((observed - expected) / tolerance, 2));
    }


    /**
     * A penalty factor used for expected intensity scaling in peak identification.  Yields no
     * penalty (factor of 1) if the value is at least the threshold value.  Otherwise, the penalty is
     * simply the ratio of the value to the threshold.
     * @param value
     * @param threshold
     * @return
     */
    public static double penaltyFactor(double value, double threshold) {
        return Math.min(1.0, value / threshold);
    }
}
