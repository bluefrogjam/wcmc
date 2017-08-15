package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.Spectrum
import edu.ucdavis.fiehnlab.math.similarity.Similarity
import edu.ucdavis.fiehnlab.math.spectrum.BinByRoundingMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra}
import edu.ucdavis.fiehnlab.util.Utilities

/**
  *
  * @param simmilarityOffset minimum similarity to be considered a match
  * @param algorithm         utilized algorith for computing the spectra similarity
  */
class SimilarityAnnotation(val simmilarityOffset: Double, val algorithm: Similarity) extends Annotate with LazyLogging {
  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param correctedSpectra
    * @param librarySpectra
    * @return
    */
  override def isMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {

    librarySpectra match {
      case x: Target =>
        correctedSpectra match {
          case y: MSSpectra =>
            val value = algorithm.compute(convertSpectra(y.spectraString), convertSpectra(x.spectrum.get.spectraString))
            logger.debug(s"computed match is: ${value}")
            val result = value > simmilarityOffset
            logger.debug(s"\t=> matches: ${result}")
            result

          case _ =>
            logger.debug("\t=> not a spectra, it's a feature!")
            false
        }

      case _ => false
    }

  }

  /**
    * converts the spectra to the similarity seach
    *
    * @param spectra
    * @return
    */
  def convertSpectra(spectra: String): Spectrum = {
    new BinByRoundingMethod().binSpectrum(Utilities.convertStringToSpectrum(spectra))
  }
}
