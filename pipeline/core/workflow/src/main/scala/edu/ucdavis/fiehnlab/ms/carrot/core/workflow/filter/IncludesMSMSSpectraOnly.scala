package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{MSMSSpectra, MSSpectra}

/**
  * only includes MSMS spectra
  */
class IncludesMSMSSpectraOnly extends Filter[MSSpectra] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: MSSpectra): Boolean = spectra.isInstanceOf[MSMSSpectra] && spectra.msLevel > 1
}
