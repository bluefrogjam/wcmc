package edu.ucdavis.fiehnlab.ms.carrot.core.api.math

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSpectra, Target}

/**
  * Created by wohlgemuth on 6/30/16.
  */
object RetentionTimeDifference {

  /**
    * computes the distance of the retention index in seconds
    * @param target
    * @param spectra
    * @return
    */
  def inSeconds(target:Target,spectra:CorrectedSpectra) :Double= {
    Math.abs(target.retentionTimeInSeconds - spectra.retentionIndex)
  }
}
