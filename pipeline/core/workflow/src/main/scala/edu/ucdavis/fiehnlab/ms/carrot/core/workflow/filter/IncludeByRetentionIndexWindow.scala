package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.CorrectedSpectra
import org.springframework.context.ApplicationContext

/**
  * includes by retention index time window
  *
  * @param timeInSeconds
  * @param window
  */
class IncludeByRetentionIndexWindow(val timeInSeconds: Double, val phaseToLog: String, val window: Double = 5) extends Filter[CorrectedSpectra] {
  /**
    * a method which should be overwritten, if the filter can provide details why it failed
    *
    * @param spectra
    * @param applicationContext
    * @return
    */
  override protected def doIncludeWithDetails(spectra: CorrectedSpectra, applicationContext: ApplicationContext): (Boolean, Any) = {
    val min = timeInSeconds - window
    val max = timeInSeconds + window

    val result = spectra.retentionIndex >= min && spectra.retentionIndex < max

    (result, Map("min" -> min, "max" -> max, "retentionIndex" -> spectra.retentionIndex))
  }

  /**
    * references to all used settings
    */
  override protected val usedSettings: Map[String, Any] = Map("retentionIndex" -> timeInSeconds, "window" -> 5)
}
