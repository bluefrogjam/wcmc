package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.CorrectedSpectra
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature

/**
  * includes by retention index time window
  *
  * @param timeInSeconds
  * @param window
  */
class IncludeByRetentionIndexTimeWindow(val timeInSeconds: Double, val window: Double = 5) extends Filter[Feature with CorrectedSpectra] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: Feature with CorrectedSpectra): Boolean = {
    val min = timeInSeconds - window
    val max = timeInSeconds + window

    spectra.retentionIndex >= min && spectra.retentionIndex < max
  }
}
