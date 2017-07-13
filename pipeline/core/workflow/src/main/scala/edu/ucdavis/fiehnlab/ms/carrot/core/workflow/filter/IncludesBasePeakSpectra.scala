package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra

/**
  * includes all spectra, having the base peak defined in the list of basePeaks, with the accuracy between Peak +/- accuracy
  *
  * @param basePeaks
  * @param accuracy
  */
class IncludesBasePeakSpectra(val basePeaks: List[Double], val accuracy: Double = 0.00005) extends Filter[MSSpectra] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: MSSpectra): Boolean = {
    basePeaks.exists { peak =>
      logger.debug(s"basePeak of spectra is ${spectra.basePeak.mass} compared to ${peak}")
      val result = peak > (spectra.basePeak.mass - accuracy) && peak < (spectra.basePeak.mass + accuracy)

      logger.debug(s"it's considered to be accepted: ${result} with an accuracy of ${accuracy}")
      result
    }
  }
}
