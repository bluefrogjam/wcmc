package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.Similarity
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra

/**
  * checks if the spectra matches the given similarity, based on the cutoff
  *
  * @param origin
  * @param cutoff needs to be less than 1
  */
class IncludeBySimilarity(val origin: MSSpectra, val cutoff: Double) extends Filter[MSSpectra] {

  assert(cutoff <= 1)

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  override def include(spectra: MSSpectra): Boolean = {
    val result = Similarity.compute(spectra, origin)

    assert(result <= 1.0)

    result >= cutoff

  }
}
