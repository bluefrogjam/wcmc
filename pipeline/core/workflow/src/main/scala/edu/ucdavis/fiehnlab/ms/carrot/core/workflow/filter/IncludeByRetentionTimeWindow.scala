package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature

/**
  * returns true, if the spectras retention time is in the defined window
  *
  * @param timeInSeconds
  * @param window
  */
class IncludeByRetentionTimeWindow(val timeInSeconds: Double, val window: Double = 5) extends Filter[Feature] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: Feature): Boolean = {
    val min = timeInSeconds - window
    val max = timeInSeconds + window

    spectra.retentionTimeInSeconds > min && spectra.retentionTimeInSeconds < max
  }
}
