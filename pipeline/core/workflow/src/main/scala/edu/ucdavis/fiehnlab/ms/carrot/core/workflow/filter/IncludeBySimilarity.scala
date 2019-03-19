package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.Similarity
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SimilaritySupport
import org.springframework.context.ApplicationContext

/**
  * checks if the spectra matches the given similarity, based on the cutoff
  *
  * @param origin
  * @param cutoff needs to be less than 1
  */
class IncludeBySimilarity(val origin: SimilaritySupport, val cutoff: Double) extends Filter[SimilaritySupport] {

  val fix_cutoff: Double = cutoff - cutoff % 0.0001

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  protected override def doIncludeWithDetails(spectra: SimilaritySupport, applicationContext: ApplicationContext): (Boolean,Any) = {
    val result = Similarity.compute(spectra, origin)

    (result >= fix_cutoff, result - result % 0.0001)

  }
}
