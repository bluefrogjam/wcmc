package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra

/**
  * includes spectra with the given level
  *
  * @param level
  */
class IncludesMSLevelSpectra(val level: Short) extends Filter[MSSpectra] {
  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: MSSpectra): Boolean = spectra.msLevel.equals(level)
}
