package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.MassAccuracy
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra

/**
  * Created by wohlgemuth on 7/12/17.
  */
class IncludeByMassRange(val mass: Double, val window: Double) extends Filter[MSSpectra] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: MSSpectra): Boolean = MassAccuracy.findClosestIon(spectra, mass).orNull != null
}
