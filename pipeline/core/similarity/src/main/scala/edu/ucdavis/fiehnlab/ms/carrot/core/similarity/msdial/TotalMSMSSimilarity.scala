package edu.ucdavis.fiehnlab.ms.carrot.core.similarity.msdial

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature

class TotalMSMSSimilarity extends MSDialSimilarity {

  /**
    * computes the limited total similarity based only on MS1 properties
    *
    * @param unknown
    * @param library
    * @return
    */
  override def doCompare(unknown: Feature, reference: Feature, tolerance: Double): Double = {

    val dotProductFactor: Int = 3
    val reverseDotProductFactor: Int = 2
    val presencePercentageFactor: Int = 1

    // combined spectral similarity
    val similarity: Double = (
      dotProductFactor * new CosineSimilarity().doCompare(unknown, reference, tolerance) +
      reverseDotProductFactor * new ReverseSimilarity().doCompare(unknown, reference, tolerance) +
      presencePercentageFactor * new PresenceSimilarity().doCompare(unknown, reference, tolerance)
    ) / (dotProductFactor + reverseDotProductFactor + presencePercentageFactor)

    similarity
  }

  /**
    * computes full total similarity based on MS1 and MS/MS properties
    * TODO: tie in with carrot data types
    * @param unknown
    * @param reference
    * @param ms1Tolerance
    * @param ms2Tolerance
    */
  def compare(unknown: Feature, reference: Feature, ms1Tolerance: Double, ms2Tolerance: Double, spectrumPenalty: Boolean, useRT: Boolean) = 0
}
