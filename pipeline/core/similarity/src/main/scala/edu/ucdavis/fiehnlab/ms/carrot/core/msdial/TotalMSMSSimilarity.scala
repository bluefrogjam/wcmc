package edu.ucdavis.fiehnlab.ms.carrot.core.msdial

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
}
