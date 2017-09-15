package edu.ucdavis.fiehnlab.ms.carrot.core.api.math

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{AccurateMassSupport, Feature, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Target}

/**
  * Created by wohlgemuth on 6/22/16.
  */
object MassAccuracy extends LazyLogging {

  /**
    * returns the ion in this spectrum, which is closest mass to the target mass, in the defined window
    *
    * @param spectra
    * @param targetMass
    * @return
    */
  def findClosestIon(spectra: Feature, targetMass: Double): Option[Ion] = {
    spectra match {
      case x: MSSpectra if x.spectrum.isDefined =>
          if(x.spectrum.get.ions.isEmpty){
            logger.warn(s"${x} has no IONS!")
            None
          }
          else {
            Some(x.spectrum.get.ions.minBy(p => Math.abs(p.mass - targetMass)))
          }

      case x: Feature =>
        x.massOfDetectedFeature
    }
  }

  def calculateMassErrorPPM(spectra: AccurateMassSupport, target: Target): Option[Double] = {
    if (target.precursorMass.isDefined) {
      val error = calculateMassError(spectra, target)

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
    * @return
    */
  def calculateMassError(spectra: AccurateMassSupport, target: Target): Option[Double] = {
    if (target.precursorMass.isDefined) {
      val mass = target.precursorMass.get

      val ion = spectra.accurateMass

      if (ion.isDefined) {
        Some(Math.abs(mass - ion.get))
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
