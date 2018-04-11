package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import org.springframework.context.ApplicationContext

/**
  * returns true, if the spectras retention time is in the defined window
  *
  * @param timeInSeconds
  * @param window
  */
class IncludeByRetentionTimeWindow(val timeInSeconds: Double, val phaseToLog: String, val window: Double = 5) extends Filter[Feature] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  protected override def doInclude(spectra: Feature, applicationContext: ApplicationContext): Boolean = {
    val min = timeInSeconds - window
    val max = timeInSeconds + window

    spectra.retentionTimeInSeconds > min && spectra.retentionTimeInSeconds < max
  }

  /**
    * references to all used settings
    */
  override protected val usedSettings: Map[String, Any] = Map("retentionTime" -> timeInSeconds, "window" -> window)
}
