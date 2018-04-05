package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra

/**
  * includes all spectra, having the base peak defined in the list of basePeaks, with the accuracy between Peak +/- accuracy
  *
  * @param basePeaks
  * @param accuracy
  */
class IncludesBasePeakSpectra(val basePeaks: Iterable[Double], val accuracy: Double = 0.00005) extends Filter[MSSpectra] {

  def isNominal: Boolean = accuracy == 0.0

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: MSSpectra): Boolean = {
    basePeaks.exists { peak =>
      logger.debug(s"basePeak of spectra is ${spectra.associatedScan.get.basePeak.mass} compared to ${peak}")

      val result = if (isNominal) {
        Math.floor(peak + 0.2) == Math.floor(spectra.associatedScan.get.basePeak.mass + 0.2)
      }
      else {
        peak > (spectra.associatedScan.get.basePeak.mass - accuracy) && peak < (spectra.associatedScan.get.basePeak.mass + accuracy)
      }


      logger.debug(s"it's considered to be accepted: ${result} with an accuracy of ${accuracy}")
      result
    }
  }
}
