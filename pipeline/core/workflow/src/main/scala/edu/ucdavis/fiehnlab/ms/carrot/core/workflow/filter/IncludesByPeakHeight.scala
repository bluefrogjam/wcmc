package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra

/**
  * Include the spectra, if any ion of it's ions has a mass between any of the required ions +/- the mass accuracy
  */
class IncludesByPeakHeight(val peaks: List[Ion], val massAccuracy: Double = 0.0005, val minIntensity: Float = 0.0f) extends Filter[MSSpectra] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: MSSpectra): Boolean = {
    peaks.foreach { ion =>
      if (ion.mass > 0) {
        spectra.ions.foreach { spectraIon =>
          if ((spectraIon.mass > ion.mass - massAccuracy) && ion.mass < (ion.mass + massAccuracy) && ion.intensity > minIntensity) {
            return true
          }
        }
      }
    }
    false
  }
}
