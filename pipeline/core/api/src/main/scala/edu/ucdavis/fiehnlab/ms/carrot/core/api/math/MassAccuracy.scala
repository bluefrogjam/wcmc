package edu.ucdavis.fiehnlab.ms.carrot.core.api.math

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Target}

/**
  * Created by wohlgemuth on 6/22/16.
  */
object MassAccuracy extends LazyLogging {

  /**
    * returns the ion in this spectra, which is closest to the target mass, in the defined window
    *
    * @param spectra
    * @param targetMass
    * @return
    */
  def findClosestIon(spectra: Feature, targetMass: Double): Option[Ion] = {
    logger.trace(s"outdated method, refactor! ${spectra.scanNumber} and looking for ${targetMass}")
    spectra match {
      case x: MSSpectra =>
        Some(x.ions.minBy(p => Math.abs(p.mass - targetMass)))
      case x: Feature =>
        x.massOfDetectedFeature
    }

  }

  def calculateMassErrorPPM(spectra: Feature, target: Target, massWindow: Double = 0): Option[Double] = {
    if (target.precursorMass.isDefined) {
      val error = calculateMassError(spectra, target, massWindow)

      if (error.isDefined) {
        Some(error.get / target.precursorMass.get * 1000000)
      }
      else {
        None
      }
    }
    else {
      None
    }
  }

  /**
    * calculates the absolute distance
    *
    * @param spectra
    * @param target
    * @param massWindow
    * @return
    */
  def calculateMassError(spectra: Feature, target: Target, massWindow: Double = 0): Option[Double] = {
    if (target.precursorMass.isDefined) {
      val mass = target.precursorMass.get

      val ion = findClosestIon(spectra, mass)

      if (ion.isDefined) {
        Some(Math.abs(mass - ion.get.mass))
      }
      else {
        None
      }

    }
    else {
      None
    }
  }
}
