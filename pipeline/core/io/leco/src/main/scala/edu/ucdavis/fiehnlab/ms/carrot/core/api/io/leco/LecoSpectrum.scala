package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.leco

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{MSSpectra, SimilaritySupport}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{IonMode, PositiveMode}

/**
  * This provides us with an easy way to read LECO gcms samples into the system
  */
trait LecoSpectrum extends MSSpectra with SimilaritySupport {

  override val ionMode:Option[IonMode] = Some(PositiveMode())

}
