package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra

/**
  * excludes all spectra with the specific base peak
  *
  * @param basePeaks
  * @param accuracy
  */
class ExcludeBasePeakSpectra(override val basePeaks: List[Double], override val accuracy: Double = 0.00005) extends IncludesBasePeakSpectra(basePeaks = basePeaks, accuracy = accuracy) {
  override def include(spectra: MSSpectra): Boolean = {
    basePeaks.exists { peak =>
      logger.debug(s"basePeak of spectra is ${spectra.basePeak.mass} compared to ${peak}")
      val result = !(peak > (spectra.basePeak.mass - accuracy) && peak < (spectra.basePeak.mass + accuracy))

      logger.debug(s"it's considered to be accepted: ${result} with an accuracy of ${accuracy}")
      result
    }
  }
}
