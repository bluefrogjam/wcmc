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
class IncludeBySimilarity(val origin: SimilaritySupport, val cutoff: Double, val phaseToLog: String) extends Filter[SimilaritySupport]  {

  assert(cutoff <= 1)

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  protected override def doIncludeWithDetails(spectra: SimilaritySupport, applicationContext: ApplicationContext): (Boolean,Any) = {
    val result = Similarity.compute(spectra, origin)

    assert(result <= 1.0)

    (result >= cutoff,result)

  }

  /**
    * references to all used settings
    */
  override protected val usedSettings: Map[String, Any] = Map("minSimilarity" -> cutoff, "target" -> origin)
}
