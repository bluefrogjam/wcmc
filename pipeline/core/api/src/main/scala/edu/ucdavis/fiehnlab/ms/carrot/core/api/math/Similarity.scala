package edu.ucdavis.fiehnlab.ms.carrot.core.api.math

import edu.ucdavis.fiehnlab.Spectrum
import edu.ucdavis.fiehnlab.math.similarity.CompositeSimilarity
import edu.ucdavis.fiehnlab.math.spectrum.BinByRoundingMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{MSSpectra, SimilaritySupport}
import edu.ucdavis.fiehnlab.util.Utilities

/**
  * Created by wohlgemuth on 6/26/16.
  */
object Similarity {

  /**
    * computes the similarity between 1 and 0 for these 2 spectra
    *
    * @param unknown
    * @param library
    * @return
    */
  def compute(unknown: SimilaritySupport, library: SimilaritySupport, algorithm: edu.ucdavis.fiehnlab.math.similarity.Similarity = new CompositeSimilarity): Double = {
    algorithm.compute(convertSpectra(unknown.spectrum.get.spectraString()), convertSpectra(library.spectrum.get.spectraString()))
  }


  /**
    * converts the spectra to the similarity seach
    *
    * @param spectra
    * @return
    */
  private def convertSpectra(spectra: String): Spectrum = {
    new BinByRoundingMethod().binSpectrum(Utilities.convertStringToSpectrum(spectra))
  }
}
