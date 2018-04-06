package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature

/**
  * Include the spectra, if any ion of it's ions has a mass between any of the required ions +/- the mass accuracy
  */
class IncludesByPeakHeight(val peaks: Iterable[Double], val massAccuracy: Double = 0.0005, val minIntensity: Float = 0.0f) extends Filter[Feature] {

  def isNominal: Boolean = massAccuracy == 0.0

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: Feature): Boolean = {
    peaks.foreach { ion =>
      if (ion > 0) {
        spectra.associatedScan.get.ions.foreach { spectraIon =>
          if (!isNominal) {
            if ((spectraIon.mass > ion - massAccuracy) && ion < (ion + massAccuracy) && spectraIon.intensity > minIntensity) {
              return true
            }
          }
          else {
            if (Math.floor(spectraIon.mass + 0.2) == Math.floor(ion + 0.2) && spectraIon.intensity > minIntensity) {
              return true
            }
          }
        }
      }
    }
    false
  }
}
