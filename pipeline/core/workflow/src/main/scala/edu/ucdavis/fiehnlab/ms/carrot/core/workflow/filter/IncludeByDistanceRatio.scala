package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import org.springframework.context.ApplicationContext

class IncludeByDistanceRatio(val targetBest: Target, val annotationBest: Feature, val targetToTest: Target, val minRatio: Double, val maxRatio: Double,val phaseToLog: String) extends Filter[Feature] {
  logger.info(s"using ${targetBest} as reference target vs ${targetToTest}")

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  protected override def doInclude(spectra: Feature,applicationContext: ApplicationContext): Boolean = {
    val distanceTargets = Math.abs(targetBest.retentionIndex - targetToTest.retentionIndex)
    val distanceAnnotation = Math.abs(annotationBest.retentionTimeInSeconds - spectra.retentionTimeInSeconds)

    /*
        logger.info(s"distance to targets is: ${distanceTargets}")
        logger.info(s"distance to annotation is: ${distanceAnnotation}")
        logger.info(s"${annotationBest.retentionTimeInSeconds} vs ${spectra.retentionTimeInSeconds}")
    */

    val ratio = distanceTargets / distanceAnnotation

    /*
        logger.info(s"ratio is: ${ratio}")
    */

    //logger.info(s"distance ratio is ${ratio} and needs to be between ${minRatio} and ${maxRatio}")

    if (targetToTest.eq(targetBest)) {
      true
    }
    else {
      ratio >= minRatio && ratio <= maxRatio
    }
  }

  /**
    * references to all used settings
    */
  override protected val usedSettings: Map[String, Any] = Map(
    "targetValidation" -> targetBest,
    "annotationValidation" -> annotationBest,
    "targetToEvaluate" -> targetBest
  )
}
