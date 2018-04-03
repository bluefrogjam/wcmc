package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature

class IncludeByDistanceRatio(val targetBest:Target, val annotationBest:Feature, val targetToTest:Target, val minRatio:Double, val maxRatio:Double) extends Filter[Feature]{
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: Feature): Boolean = {
    val distanceTargets = Math.abs(targetBest.retentionIndex - targetToTest.retentionIndex)
    val distanceAnnotation = Math.abs(annotationBest.retentionTimeInSeconds - spectra.retentionTimeInSeconds)

    //logger.info(s"distance to targets is: ${distanceTargets}")
    //logger.info(s"distance to annoation is: ${distanceAnnotation}")

    val ratio = distanceTargets/distanceAnnotation

    //logger.info(s"distance ratio is ${ratio} and needs to be between ${minRatio} and ${maxRatio}")
    ratio >= minRatio && ratio <= maxRatio
  }
}
