package edu.ucdavis.fiehnlab.ms.carrot.math

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature

object SimilarityMethods extends Logging {
  /**
    * Gaussian similarity used for mass accuracy and retention time similarity calculations
    *
    * @param observed
    * @param expected
    * @param tolerance
    * @return
    */
  def gaussianSimilarity(observed: Double, expected: Double, tolerance: Double): Double = {
    Math.exp(-0.5 * Math.pow((observed - expected) / tolerance, 2))
  }


  /**
    * A penalty factor used for expected intensity scaling in peak identification.  Yields no
    * penalty (factor of 1) if the value is at least the threshold value.  Otherwise, the penalty is
    * simply the ratio of the value to the threshold.
    *
    * @param value
    * @param threshold
    * @return
    */
  def penaltyFactor(value: Double, threshold: Double): Double = {
    Math.min(1.0, value / threshold)
  }

  /**
    * Calculates the gaussian similarity between a detected feature and a target using m/z and RT similarity
    * @param feature
    * @param target
    * @param mzTolerance
    * @param rtTolerance
    * @return
    */
  def featureTargetSimilarity(feature: Feature, target: Target, mzTolerance: Double, rtTolerance: Double): Double = {
    if (feature.accurateMass.isDefined && target.precursorMass.isDefined) {
      val mzSimilarity = gaussianSimilarity(feature.accurateMass.get, target.precursorMass.get, mzTolerance) * 1.2
      val rtSimilarity = gaussianSimilarity(feature.retentionTimeInSeconds, target.retentionIndex, rtTolerance) * 0.8

      logger.debug(f"massTolerance: ${mzTolerance}%.2f -- mz similarity: ${mzSimilarity}%.4f -- rt similarity: ${rtSimilarity}%.2f")

      (mzSimilarity + rtSimilarity) / 2
    } else {
      0.0
    }
  }

  /**
    * Calculates the gaussian similarity between a detected feature and a target using m/z and RT similarity
    * and then applying a penalty factor based on the feature intensity
    * @param feature
    * @param target
    * @param mzTolerance
    * @param rtTolerance
    * @param intensityThreshold
    * @return
    */
  def featureTargetSimilarity(feature: Feature, target: Target, mzTolerance: Double, rtTolerance: Double, intensityThreshold: Double): Double = {
    if (feature.massOfDetectedFeature.isDefined) {
      val intensityPenalty = penaltyFactor(feature.massOfDetectedFeature.get.intensity, intensityThreshold)
      logger.info(f"Feature (${feature.massOfDetectedFeature.get.mass}%.4f@${feature.retentionTimeInSeconds}%.2f)'s intensity: ${feature.massOfDetectedFeature.get.intensity}%.0f -- threshold: ${intensityThreshold}%.0f -- penalty factor: ${intensityPenalty}%.4f")

      intensityPenalty * featureTargetSimilarity(feature, target, mzTolerance, rtTolerance)
    } else {
      featureTargetSimilarity(feature, target, mzTolerance, rtTolerance)
    }
  }
}
