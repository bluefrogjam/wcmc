package edu.ucdavis.fiehnlab.ms.carrot.core.similarity.nominal

import edu.ucdavis.fiehnlab.ms.carrot.core.similarity.{NominalMassSimilarity, Similarity, SpectrumSimilarityUtilities}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion

class NominalCosineSimilarity extends NominalMassSimilarity {

  /**
    * computes the simple, nominal mass cosine similarity of two spectra
    *
    * @param unknown
    * @param library
    * @return
    */
  override def doCompare(unknown: Feature, reference: Feature): Double = {

    val unknownSpectrum: Map[Int, Ion] = SpectrumSimilarityUtilities.convertToNominal(unknown)
    val referenceSpectrum: Map[Int, Ion] = SpectrumSimilarityUtilities.convertToNominal(unknown)
    val sharedIons: Seq[Int] = (unknownSpectrum.keySet intersect referenceSpectrum.keySet).toSeq

    compute(unknownSpectrum, referenceSpectrum, sharedIons)
  }

  def compute(unknown: Map[Int, Ion], reference: Map[Int, Ion], sharedIons: Seq[Int]): Double = {
    val unknownNorm: Double = math.sqrt(unknown.values.map(x => x.intensity * x.intensity).sum)
    val libraryNorm: Double = math.sqrt(reference.values.map(x => x.intensity * x.intensity).sum)
    val product: Double = sharedIons.map(k => unknown(k).intensity * reference(k).intensity).sum

    product / libraryNorm / unknownNorm
  }
}
