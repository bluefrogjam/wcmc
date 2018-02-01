package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Target}

/**
  * considered to be a match, if the accurate mass of the spectra is in the range of
  *
  * @param massAccuracyInDalton
  */
class AccurateMassAnnotation(massAccuracyInDalton: Double, minIntensity: Float = 0, val phase: String) extends Annotate with LazyLogging {

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param correctedSpectra
    * @param librarySpectra
    * @return
    */
  override def doMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {
    librarySpectra.precursorMass match {
      case Some(mass) =>
        logger.trace(s"checking mass: ${mass}")

        val min = mass - massAccuracyInDalton
        val max = mass + massAccuracyInDalton

        logger.trace(s"\t=> min: ${min} and max: ${max} ")

        correctedSpectra match {

          case x: Feature if x.massOfDetectedFeature.isDefined =>
            val ion = x.massOfDetectedFeature.get

            logger.trace(s"\t\t=> ion mass is ${ion.mass} and intensity is ${ion.intensity}")

            val result = (ion.mass >= min) && (ion.mass <= max) && ion.intensity >= minIntensity

            logger.trace(s"\t\t\t=> matches: ${result}")
            result

          case _ => false
        }
      case None =>
        logger.trace(s"no spectra was provided for given library spectra: $librarySpectra")
        false
    }
  }

  /**
    * which phase we require to log
    */
  override protected val phaseToLog = phase
  /**
    * references to all used settings
    */
  override protected val usedSettings = Map("minIntensity" -> minIntensity, "massAccuracyInDalton" -> massAccuracyInDalton)
}

/**
  * considered to be a match if the intensity of the given peak is larger than X. These are absolute values
  *
  * @param massAccuracyInDalton
  */
class MassIsHighEnoughAnnotation(massAccuracyInDalton: Double, minIntensity: Float, val phase: String) extends Annotate with LazyLogging {

  /**
    * references to all used settings
    */
  override protected val usedSettings = Map("minIntensity" -> minIntensity, "massAccuracyInDalton" -> massAccuracyInDalton)

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param correctedSpectra
    * @param librarySpectra
    * @return
    */
  override def doMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {
    librarySpectra.precursorMass match {
      case Some(mass) =>
        logger.trace(s"checking mass: ${mass}")

        val min = mass - massAccuracyInDalton
        val max = mass + massAccuracyInDalton

        logger.trace(s"\t=> min: ${min} and max: ${max} ")

        correctedSpectra match {
          case x: Feature if x.massOfDetectedFeature.isDefined =>
            val ion = x.massOfDetectedFeature.get

            logger.trace(s"\t\t=> ion mass is ${ion.mass} and intensity is ${ion.intensity}")

            val result = (ion.mass >= min) && (ion.mass <= max) && ion.intensity >= minIntensity

            logger.trace(s"\t\t\t=> matches: ${result}")
            result

          case _ => false
        }

      case None =>
        logger.trace(s"no spectra was provided for given library spectra: $librarySpectra")
        false
    }
  }


  /**
    * which phase we require to log
    */
  override protected val phaseToLog = phase
}

/**
  * considred to be a match, if the accurate mass of the spectra is in the provided range and has a
  * certain mass accuracy in dalton
  *
  * @param massAccuracyInDalton
  */
class AccurateMassBasePeakAnnotation(massAccuracyInDalton: Double, phase: String) extends AccurateMassAnnotation(massAccuracyInDalton, 100, phase)

/**
  * considered to be a match, if the accuracte mass of the spectra is in the provided ppm window
  *
  * @param massAccuracyInPPM
  */
class AccurateMassAnnotationPPM(massAccuracyInPPM: Int, val phase: String) extends Annotate with LazyLogging {

  logger.debug(s"mass accuracy: ${massAccuracyInPPM} ppm")

  /**
    * references to all used settings
    */
  override protected val usedSettings = Map("massAccuracyInPPM" -> massAccuracyInPPM)

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param correctedSpectra
    * @param librarySpectra
    * @return
    */
  override def doMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {
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


  /**
    * which phase we require to log
    */
  override protected val phaseToLog = phase
}


/**
  * considered to be a match, if the accurate mass of the spectra is in the range of
  *
  * @param massAccuracyInDalton
  */
class AccurateMassInSpectraAnnotation(massAccuracyInDalton: Double, minIntensity: Float = 0, val phase: String) extends Annotate with LazyLogging {

  /**
    * references to all used settings
    */
  override protected val usedSettings = Map("minIntensity" -> minIntensity, "massAccuracyInDalton" -> massAccuracyInDalton)

  logger.info(s"utilizing accuracy of ${massAccuracyInDalton}")

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param correctedSpectra
    * @param librarySpectra
    * @return
    */
  override def doMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {
    librarySpectra.precursorMass match {
      case Some(mass) =>
        logger.debug(s"checking mass: ${mass}")

        val min = mass - massAccuracyInDalton
        val max = mass + massAccuracyInDalton

        logger.debug(s"\t=> min: ${min} and max: ${max} ")

        correctedSpectra match {
          case x: MSSpectra if x.associatedScan.isDefined =>
            x.associatedScan.get.relativeSpectra.exists { ion: Ion =>

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


  /**
    * which phase we require to log
    */
  override protected val phaseToLog = phase
}

/**
  * considered to be a match if the intensity of the given peak is larger than X. These are absolute values
  *
  * @param massAccuracyInDalton
  */
class MassIsHighEnoughInSpectraAnnotation(massAccuracyInDalton: Double, minIntensity: Float, val phase: String) extends Annotate with LazyLogging {

  logger.info(s"utilizing accuracy of ${massAccuracyInDalton}")

  /**
    * references to all used settings
    */
  override protected val usedSettings = Map("minIntensity" -> minIntensity, "massAccuracyInDalton" -> massAccuracyInDalton)

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param correctedSpectra
    * @param librarySpectra
    * @return
    */
  override def doMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {
    librarySpectra.precursorMass match {
      case Some(mass) =>
        logger.trace(s"checking mass: ${mass}")

        val min = mass - massAccuracyInDalton
        val max = mass + massAccuracyInDalton

        logger.trace(s"\t=> min: ${min} and max: ${max} ")

        correctedSpectra match {
          case x: MSSpectra if x.associatedScan.isDefined =>
            x.associatedScan.get.ions.exists { ion: Ion =>

              logger.trace(s"\t\t=> ion mass is ${ion.mass} and intensity is ${ion.intensity}")

              val result = (ion.mass >= min) && (ion.mass <= max) && ion.intensity >= minIntensity

              logger.trace(s"\t\t\t=> matches: ${result}")
              result
            }

          case _ => false
        }

      case None =>
        logger.debug(s"no spectra was provided for given library spectra: $librarySpectra")
        false
    }
  }


  /**
    * which phase we require to log
    */
  override protected val phaseToLog = phase
}

/**
  * considered to be a match, if the accuracte mass of the spectra is in the provided ppm window
  *
  * @param massAccuracyInPPM
  */
class AccurateMassInSpectraAnnotationPPM(massAccuracyInPPM: Int, val phase: String) extends Annotate with LazyLogging {

  /**
    * references to all used settings
    */
  override protected val usedSettings = Map("massAccuracyInPPM" -> massAccuracyInPPM)

  /**
    * returns true, if the corrected spectra is considered to be a match for the library spectra
    *
    * @param correctedSpectra
    * @param librarySpectra
    * @return
    */
  override def doMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {
    librarySpectra.precursorMass match {
      case Some(mass) =>

        correctedSpectra match {
          case x: MSSpectra if x.associatedScan.isDefined =>
            logger.trace(s"checking mass: ${mass}")

            x.associatedScan.get.ions.exists { ion: Ion =>
              val error = mass - ion.mass
              val ppm = Math.abs(error / mass * 1000000)
              logger.trace(s"\t=> error: ${error} and ppm: ${ppm}")
              val result = ppm < massAccuracyInPPM
              logger.trace(s"\t\t=> matches: ${result}")
              result
            }

          case _ => false
        }

      case None =>
        logger.trace(s"no spectra was provided for given library spectra: $librarySpectra")
        false
    }
  }


  /**
    * which phase we require to log
    */
  override protected val phaseToLog = phase
}
