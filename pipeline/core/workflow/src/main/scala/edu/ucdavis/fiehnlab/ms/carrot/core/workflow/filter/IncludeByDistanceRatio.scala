package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.diagnostics.JSONTargetLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import org.springframework.context.ApplicationContext

class IncludeByDistanceRatio(val targetBest: Target, val annotationBest: Feature, val targetToTest: Target, val minRatio: Double, val maxRatio: Double, val phaseToLog: String) extends Filter[Feature] with JSONTargetLogging {
  logger.info(s"using ${targetBest} as reference target vs ${targetToTest}")

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  protected override def doIncludeWithDetails(spectra: Feature, applicationContext: ApplicationContext): (Boolean, Any) = {
    val distanceTargets = Math.abs(targetBest.retentionIndex - targetToTest.retentionIndex)
    val distanceAnnotation = Math.abs(annotationBest.retentionTimeInSeconds - spectra.retentionTimeInSeconds)
    val ratio = distanceTargets / distanceAnnotation

    //logger.info(s"distance ratio is ${ratio} and needs to be between ${minRatio} and ${maxRatio}")

    if (targetToTest.eq(targetBest)) {
      (true, "validation target is the same target as the target to test")
    }
    else {
      (ratio >= minRatio && ratio <= maxRatio, Map("ratio" -> ratio, "distanceTargets" -> distanceTargets, "distanceAnnotations" -> distanceAnnotation))
    }
  }

  /**
    * references to all used settings
    */
  override protected val usedSettings: Map[String, Any] = Map(
    "targetValidation" -> targetBest,
    "annotationValidation" -> annotationBest
  )
  /**
    * which target we require to log
    */
  override protected val targetToLog: Target = targetToTest
}
