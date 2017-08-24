package edu.ucdavis.fiehnlab.ms.carrot.core.api.math

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.CorrectedSpectra

/**
  * Created by wohlgemuth on 6/30/16.
  */
object RetentionIndexDifference {

  /**
    * computes the distance of the retention index in seconds
    * @param target
    * @param spectra
    * @return
    */
  def diff(target:Target, spectra:CorrectedSpectra) :Double= {
    Math.abs(target.retentionIndex - spectra.retentionIndex)
  }
}
