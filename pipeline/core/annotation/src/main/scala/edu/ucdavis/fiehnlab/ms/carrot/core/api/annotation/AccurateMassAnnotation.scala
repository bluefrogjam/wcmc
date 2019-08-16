package edu.ucdavis.fiehnlab.ms.carrot.core.api.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.MassAccuracy
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Target}
import org.apache.logging.log4j.scala.Logging

/**
  * considered to be a match, if the accurate mass of the spectra is in the range of
  *
  * @param massAccuracyInDalton
  */
class AccurateMassAnnotation(massAccuracyInDalton: Double, minIntensity: Float = 0) extends Annotate with Logging {

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

}

/**
  * considered to be a match if the intensity of the given peak is larger than X. These are absolute values
  *
  * @param massAccuracyInDalton
  */
class MassIsHighEnoughAnnotation(massAccuracyInDalton: Double, minIntensity: Float) extends Annotate with Logging {

  /**
    * references to all used settings
    */

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


}

/**
  * we accept if either the mass accuracy is in the correct window for ppm or Da
  *
  * @param massAccuracyInPPM
  * @param massAccuracyInDa
  */
class MassAccuracyPPMorDalton(massAccuracyInPPM: Double, massAccuracyInDa: Double, minIntensity: Double = 0.0) extends Annotate with Logging {
  assert(massAccuracyInDa < 1, "needs to be below 1, no sense in checking for 1 dalton precession in 2019")
  assert(massAccuracyInPPM >= 1, "ppm needs to be larger/equal 1")
  assert(minIntensity >= 0, "min intensity needs to be >= 0")

  override def doMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {
    librarySpectra.precursorMass match {
      case Some(mass) =>


        correctedSpectra match {
          case x: Feature if x.massOfDetectedFeature.isDefined =>

            val ppmError = MassAccuracy.calculateMassErrorPPM(x, librarySpectra)
            val massError = MassAccuracy.calculateMassError(x, librarySpectra)

            // check mass accuracy in Da first
            if (massError.isDefined && massError.get <= massAccuracyInDa && x.massOfDetectedFeature.get.intensity > minIntensity) {
              true
            }
            else if (ppmError.isDefined && ppmError.get <= massAccuracyInPPM && x.massOfDetectedFeature.get.intensity > minIntensity) {
              true
            }
            else {
              false
            }
          case x =>
            logger.warn(f"correctedSpectra matching special situation, mass: ${mass}%.4f, feature: ${x}")
            false
        }

      case None =>
        logger.warn(s"no spectra was provided for given library spectra: $librarySpectra")
        false
    }
  }
}

/**
  * Mass accuracy in Da only annotation
  *
  * @param massAccuracyInDa
  * @param minIntensity
  */
class MassAccuracyDalton(massAccuracyInDa: Double, minIntensity: Double = 0.0) extends Annotate with Logging {
  assert(massAccuracyInDa < 1, "needs to be below 1, no sense in checking for 1 dalton precession in 2019")
  assert(minIntensity >= 0, "min intensity needs to be >= 0")

  override def doMatch(correctedSpectra: Feature, librarySpectra: Target): Boolean = {
    librarySpectra.precursorMass match {
      case Some(mass) =>


        correctedSpectra match {
          case x: Feature if x.massOfDetectedFeature.isDefined =>

            val massError = MassAccuracy.calculateMassError(x, librarySpectra)

            if (massError.isDefined && massError.get <= massAccuracyInDa && x.massOfDetectedFeature.get.intensity > minIntensity) {
              true
            }
            else {
              false
            }
          case x =>
            logger.warn(f"correctedSpectra matching special situation, mass: ${mass}%.4f, feature: ${x}")
            false
        }

      case None =>
        logger.warn(s"no spectra was provided for given library spectra: $librarySpectra")
        false
    }
  }
}

class MassIsHighEnoughAnnotationPPM(massAccuracyInPPM: Double, minIntensity: Float) extends Annotate with Logging {

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

            val error = MassAccuracy.calculateMassErrorPPM(correctedSpectra, librarySpectra)

            if (error.isDefined) {
              if (error.get <= massAccuracyInPPM) {
                correctedSpectra.massOfDetectedFeature.get.intensity > minIntensity
              }
              else {
                false
              }
            }
            else {
              false
            }
          case _ => false
        }

      case None =>
        logger.trace(s"no spectra was provided for given library spectra: $librarySpectra")
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
class AccurateMassAnnotationPPM(massAccuracyInPPM: Double) extends Annotate with Logging {

  logger.debug(s"mass accuracy: ${massAccuracyInPPM} ppm")

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


}


/**
  * considered to be a match, if the accurate mass of the spectra is in the range of
  *
  * @param massAccuracyInDalton
  */
class AccurateMassInSpectraAnnotation(massAccuracyInDalton: Double, minIntensity: Float = 0) extends Annotate with Logging {

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


}

/**
  * considered to be a match if the intensity of the given peak is larger than X. These are absolute values
  *
  * @param massAccuracyInDalton
  */
class MassIsHighEnoughInSpectraAnnotation(massAccuracyInDalton: Double, minIntensity: Float) extends Annotate with Logging {

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
  //  override protected val phaseToLog = phase
}

/**
  * considered to be a match, if the accuracte mass of the spectra is in the provided ppm window
  *
  * @param massAccuracyInPPM
  */
class AccurateMassInSpectraAnnotationPPM(massAccuracyInPPM: Int) extends Annotate with Logging {


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
}
