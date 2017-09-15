package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Target}

/**
  * considered to be a match, if the accurate mass of the spectra is in the range of
  *
  * @param massAccuracyInDalton
  */
class AccurateMassAnnotation(massAccuracyInDalton: Double, minIntensity: Float = 0) extends Annotate with LazyLogging {

  logger.info(s"utilizing accuracy of ${massAccuracyInDalton}")

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param correctedSpectra
    * @param librarySpectra
    * @return
    */
  override def isMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {
    librarySpectra.precursorMass match {
      case Some(mass) =>
        logger.debug(s"checking mass: ${mass}")

        val min = mass - massAccuracyInDalton
        val max = mass + massAccuracyInDalton

        logger.debug(s"\t=> min: ${min} and max: ${max} ")

        correctedSpectra match {

          case x: Feature if x.massOfDetectedFeature.isDefined =>
            val ion = x.massOfDetectedFeature.get

            logger.debug(s"\t\t=> ion mass is ${ion.mass} and intensity is ${ion.intensity}")

            val result = (ion.mass >= min) && (ion.mass <= max) && ion.intensity >= minIntensity

            logger.debug(s"\t\t\t=> matches: ${result}")
            result

          case _ => false
        }
      case None =>
        logger.debug(s"no spectra was provided for given library spectra: $librarySpectra")
        false
    }
  }
}

/**
  * considered to be a match if the intensity of the given peak is larger than X. These are absolute values
  *
  * @param massAccuracyInDalton
  */
class MassIsHighEnoughAnnotation(massAccuracyInDalton: Double, minIntensity: Float) extends Annotate with LazyLogging {

  logger.info(s"utilizing accuracy of ${massAccuracyInDalton}")

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param correctedSpectra
    * @param librarySpectra
    * @return
    */
  override def isMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {
    librarySpectra.precursorMass match {
      case Some(mass) =>
        logger.debug(s"checking mass: ${mass}")

        val min = mass - massAccuracyInDalton
        val max = mass + massAccuracyInDalton

        logger.debug(s"\t=> min: ${min} and max: ${max} ")

        correctedSpectra match {
          case x: Feature if x.massOfDetectedFeature.isDefined =>
            val ion = x.massOfDetectedFeature.get

            logger.debug(s"\t\t=> ion mass is ${ion.mass} and intensity is ${ion.intensity}")

            val result = (ion.mass >= min) && (ion.mass <= max) && ion.intensity >= minIntensity

            logger.debug(s"\t\t\t=> matches: ${result}")
            result

          case _ => false
        }

      case None =>
        logger.debug(s"no spectra was provided for given library spectra: $librarySpectra")
        false
    }
  }
}

/**
  * considred to be a match, if the accurate mass of the spectra is in the provided range and has a
  * certain mass accuracy in dalton
  *
  * @param massAccuracyInDalton
  */
class AccurateMassBasePeakAnnotation(massAccuracyInDalton: Double) extends AccurateMassAnnotation(massAccuracyInDalton, 100)

/**
  * considered to be a match, if the accuracte mass of the spectra is in the provided ppm window
  *
  * @param massAccuracyInPPM
  */
class AccurateMassAnnotationPPM(massAccuracyInPPM: Int) extends Annotate with LazyLogging {

  logger.debug(s"mass accuracy: ${massAccuracyInPPM} ppm")

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param correctedSpectra
    * @param librarySpectra
    * @return
    */
  override def isMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {
    librarySpectra.precursorMass match {
      case Some(mass) =>

        correctedSpectra match {

          case x: Feature if x.massOfDetectedFeature.isDefined =>
            val ion = x.massOfDetectedFeature.get
            val error = mass - ion.mass
            val ppm = Math.abs(error / mass * 1000000)
            logger.debug(s"\t=> error: ${error} and ppm: ${ppm}")
            val result = ppm < massAccuracyInPPM
            logger.debug(s"\t\t=> matches: ${result}")
            result

          case _ => false
        }

      case None =>
        logger.debug(s"no spectra was provided for given library spectra: $librarySpectra")
        false
    }
  }
}


/**
  * considered to be a match, if the accurate mass of the spectra is in the range of
  *
  * @param massAccuracyInDalton
  */
class AccurateMassInSpectraAnnotation(massAccuracyInDalton: Double, minIntensity: Float = 0) extends Annotate with LazyLogging {

  logger.info(s"utilizing accuracy of ${massAccuracyInDalton}")

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param correctedSpectra
    * @param librarySpectra
    * @return
    */
  override def isMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {
    librarySpectra.precursorMass match {
      case Some(mass) =>
        logger.debug(s"checking mass: ${mass}")

        val min = mass - massAccuracyInDalton
        val max = mass + massAccuracyInDalton

        logger.debug(s"\t=> min: ${min} and max: ${max} ")

        correctedSpectra match {
          case x: MSSpectra if x.spectrum.isDefined =>
            x.spectrum.get.relativeSpectra.exists { ion: Ion =>

              logger.debug(s"\t\t=> ion mass is ${ion.mass} and intensity is ${ion.intensity}")

              val result = (ion.mass >= min) && (ion.mass <= max) && ion.intensity >= minIntensity

              logger.debug(s"\t\t\t=> matches: ${result}")
              result
            }

          case _ => false
        }
      case None =>
        logger.debug(s"no spectra was provided for given library spectra: $librarySpectra")
        false
    }
  }
}

/**
  * considered to be a match if the intensity of the given peak is larger than X. These are absolute values
  *
  * @param massAccuracyInDalton
  */
class MassIsHighEnoughInSpectraAnnotation(massAccuracyInDalton: Double, minIntensity: Float) extends Annotate with LazyLogging {

  logger.info(s"utilizing accuracy of ${massAccuracyInDalton}")

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param correctedSpectra
    * @param librarySpectra
    * @return
    */
  override def isMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {
    librarySpectra.precursorMass match {
      case Some(mass) =>
        logger.debug(s"checking mass: ${mass}")

        val min = mass - massAccuracyInDalton
        val max = mass + massAccuracyInDalton

        logger.debug(s"\t=> min: ${min} and max: ${max} ")

        correctedSpectra match {
          case x: MSSpectra if x.spectrum.isDefined =>
            x.spectrum.get.ions.exists { ion: Ion =>

              logger.debug(s"\t\t=> ion mass is ${ion.mass} and intensity is ${ion.intensity}")

              val result = (ion.mass >= min) && (ion.mass <= max) && ion.intensity >= minIntensity

              logger.debug(s"\t\t\t=> matches: ${result}")
              result
            }

          case _ => false
        }

      case None =>
        logger.debug(s"no spectra was provided for given library spectra: $librarySpectra")
        false
    }
  }
}

/**
  * considered to be a match, if the accuracte mass of the spectra is in the provided ppm window
  *
  * @param massAccuracyInPPM
  */
class AccurateMassInSpectraAnnotationPPM(massAccuracyInPPM: Int) extends Annotate with LazyLogging {

  logger.debug(s"mass accuracy: ${massAccuracyInPPM} ppm")

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param correctedSpectra
    * @param librarySpectra
    * @return
    */
  override def isMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {
    librarySpectra.precursorMass match {
      case Some(mass) =>

        correctedSpectra match {
          case x: MSSpectra if x.spectrum.isDefined =>
            logger.debug(s"checking mass: ${mass}")

            x.spectrum.get.ions.exists { ion: Ion =>
              val error = mass - ion.mass
              val ppm = Math.abs(error / mass * 1000000)
              logger.debug(s"\t=> error: ${error} and ppm: ${ppm}")
              val result = ppm < massAccuracyInPPM
              logger.debug(s"\t\t=> matches: ${result}")
              result
            }

          case _ => false
        }

      case None =>
        logger.debug(s"no spectra was provided for given library spectra: $librarySpectra")
        false
    }
  }
}
