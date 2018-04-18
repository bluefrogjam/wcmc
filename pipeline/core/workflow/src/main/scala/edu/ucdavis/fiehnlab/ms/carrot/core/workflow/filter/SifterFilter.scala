package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.diagnostics.JSONTargetLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{MSSpectra, SimilaritySupport}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms.annotation.GCMSAnnotationLibraryProperties
import org.springframework.context.ApplicationContext

/**
  * evaluates if the given spectra matches one of the properties
  * @param phaseToLog
  * @param properties
  * @param target
  */
class SifterFilter(override val phaseToLog: String, val properties: GCMSAnnotationLibraryProperties, val target: Target) extends Filter[MSSpectra with SimilaritySupport] with JSONTargetLogging {
  /**
    * references to all used settings
    */
  override protected val usedSettings: Map[String, Any] = Map("properties" -> properties)

  /**
    *
    * @param spectra
    * @param applicationContext
    * @return
    */
  override protected def doIncludeWithDetails(spectra: MSSpectra with SimilaritySupport, applicationContext: ApplicationContext): (Boolean, Any) = {

    (false, null)
  }

  /**
    * which target we require to log
    */
  override protected val targetToLog: Target = target
}
