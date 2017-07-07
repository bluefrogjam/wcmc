package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.leco

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{IonMode, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra

/**
  * This provides us with an easy way to read LECO gcms samples into the system
  */
trait LecoSpectrum extends MSSpectra{

  override val ionMode:Option[IonMode] = Some(PositiveMode())

}
