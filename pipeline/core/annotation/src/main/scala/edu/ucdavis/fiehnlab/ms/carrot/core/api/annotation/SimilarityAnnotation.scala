package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.Spectrum
import edu.ucdavis.fiehnlab.math.similarity.Similarity
import edu.ucdavis.fiehnlab.math.spectrum.BinByRoundingMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSMSSpectra, MSSpectra}
import edu.ucdavis.fiehnlab.util.Utilities

/**
  *
  * @param simmilarityOffset minimum similarity to be considered a match
  * @param algorithm         utilized algorith for computing the spectra similarity
  */
class SimilarityAnnotation(val simmilarityOffset: Double, val algorithm: Similarity, val phase: String) extends Annotate with LazyLogging {

  override protected val usedSettings = Map("minSimilarity" -> simmilarityOffset, "algorithm" -> algorithm.getClass.getSimpleName)

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param correctedSpectra
    * @param librarySpectra
    * @return
    */
  override def doMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {

    librarySpectra match {
      case x: Target =>
        correctedSpectra match {
          case y: MSMSSpectra if y.associatedScan.isDefined =>
            val value = algorithm.compute(convertSpectra(y.spectrum.get.spectraString), convertSpectra(x.spectrum.get.spectraString))
            logger.trace(s"computed match is: ${value}")
            val result = value > simmilarityOffset
            logger.trace(s"\t=> matches: ${result}")
            result

          case _ =>
            logger.trace("\t=> not a spectra, it's a feature!")
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

  /**
    * which phase we require to log
    */
  override protected val phaseToLog = phase
}
