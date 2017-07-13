package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSLibrarySpectra

/**
  * is the quant ion included in the given spectra
  *
  * @param librarySpectra related library spectra
  * @param massAccuracy
  */
class IncludesQuantIon(librarySpectra: MSLibrarySpectra, massAccuracy: Double = 0.00005) extends IncludesByPeakHeight(List {
  Ion(librarySpectra.quantificationIon.getOrElse(0), 0)
}, massAccuracy, 0.03f)
