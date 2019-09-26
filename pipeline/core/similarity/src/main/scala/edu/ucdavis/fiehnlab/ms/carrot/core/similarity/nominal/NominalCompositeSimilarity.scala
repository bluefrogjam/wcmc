package edu.ucdavis.fiehnlab.ms.carrot.core.similarity.nominal

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import edu.ucdavis.fiehnlab.ms.carrot.core.similarity.{NominalMassSimilarity, Similarity, SpectrumSimilarityUtilities}

class NominalCompositeSimilarity extends NominalMassSimilarity {

  /**
    * computes the composite similarity of two spectra, based on:
    * http://ac.els-cdn.com/1044030594850224/1-s2.0-1044030594850224-main.pdf?_tid=4c05468e-c550-11e5-9321-00000aab0f02&acdnat=1453938616_682e264082fe2b8b1b7ef53d90e46a6b

    * @param unknown
    * @param library
    * @return
    */
  override def doCompare(unknown: Feature, reference: Feature): Double = {

    val unknownSpectrum: Map[Int, Ion] = SpectrumSimilarityUtilities.convertToNominal(unknown)
    val referenceSpectrum: Map[Int, Ion] = SpectrumSimilarityUtilities.convertToNominal(unknown)
    val sharedIons: Seq[Int] = (unknownSpectrum.keySet intersect referenceSpectrum.keySet).toSeq.sorted

    // calculate cosine similarity as the base for the composite similarity
    val cosineSimilarity = new NominalCosineSimilarity().compute(unknownSpectrum, referenceSpectrum, sharedIons)

    if (sharedIons.size > 1) {
      // Takes the ratio of successive list elements, ie A[i] / A[i + 1]
      val unknownRatios: Seq[Float] = sharedIons.map(k => unknownSpectrum(k).intensity).sliding(2).map { case List(x, y) => x / y }.toSeq
      val libraryRatios: Seq[Float] = sharedIons.map(k => referenceSpectrum(k).intensity).sliding(2).map { case List(x, y) => x / y }.toSeq

      // Divide the unknown ratio by the library ratio
      val combinedRatios: Seq[Double] = unknownRatios.zip(libraryRatios).map { case (x, y) => 1.0 * x / y }

      // Ensure each term is less than 1 and then sum
      val intensitySimilarity: Double = 1 + combinedRatios.map { x => if (x < 1) x else 1 / x }.sum

      // Compute the composite similarity
      (unknownSpectrum.size * cosineSimilarity + intensitySimilarity) / (unknownSpectrum.size + sharedIons.size)
    } else {
      cosineSimilarity
    }
  }
}
