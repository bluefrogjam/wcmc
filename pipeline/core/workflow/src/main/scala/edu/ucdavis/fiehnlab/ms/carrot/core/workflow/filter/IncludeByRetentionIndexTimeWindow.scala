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
class IncludeByRetentionIndexTimeWindow(val timeInSeconds: Double, val phaseToLog: String, val window: Double = 5) extends Filter[CorrectedSpectra] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  protected override def doInclude(spectra: CorrectedSpectra, applicationContext: ApplicationContext): Boolean = {
    val min = timeInSeconds - window
    val max = timeInSeconds + window

    spectra.retentionIndex >= min && spectra.retentionIndex < max
  }

  /**
    * references to all used settings
    */
  override protected val usedSettings: Map[String, Any] = Map("retentionIndex" -> timeInSeconds, "window" -> 5)
}
