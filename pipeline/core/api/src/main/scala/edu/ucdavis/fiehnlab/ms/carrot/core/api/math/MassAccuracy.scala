package edu.ucdavis.fiehnlab.ms.carrot.core.api.math

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{AccurateMassSupport, Feature}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Target}
import org.apache.logging.log4j.scala.Logging

/**
  * Created by wohlgemuth on 6/22/16.
  */
object MassAccuracy extends Logging {

  /**
    * returns the ion in this spectrum, which is closest mass to the target mass, in the defined window
    *
    * @param spectra
    * @param targetMass
    * @return
    */
  def findClosestIon(spectra: Feature, targetMass: Double, target: Target): Option[Ion] = {
    spectra match {
      case x: Feature if x.associatedScan.isDefined =>
          if(x.associatedScan.get.ions.isEmpty){
            logger.warn(s"${x} has no IONS!")
            None
          }
          else {
            val closest = x.associatedScan.get.ions.minBy(p => Math.abs(p.mass - targetMass))
            val refMass = new AccurateMassSupport {
              override def accurateMass: Option[Double] = Some(closest.mass)
            }
            if (calculateMassError(refMass, target).get <= 0.01) {
              Some(closest)
            } else {
              None
            }
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
