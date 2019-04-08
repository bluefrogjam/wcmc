package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.MassAccuracy
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.AccurateMassSupport
import org.apache.logging.log4j.scala.Logging
import org.springframework.context.ApplicationContext

/**
  * Created by wohlgemuth on 7/12/17.
  */
class IncludeByMassRangePPM(val target: Target, val windowInPPM: Double) extends Filter[AccurateMassSupport] with Logging {
  logger.debug(s"mass window is ${windowInPPM} for ${target}")

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  protected override def doInclude(spectra: AccurateMassSupport, applicationContext: ApplicationContext): Boolean = {
    val error = MassAccuracy.calculateMassErrorPPM(spectra, target)
    logger.debug(f"mass error is: ${error.get}%1.5f for ${spectra} and ${target} - window: ${windowInPPM} ppm")

    if (error.isDefined) {
      val result = error.get <= windowInPPM

      if (!result) {
        logger.debug(s"\thigh mass error (PPM): ${error.get} for a window max: ${windowInPPM}")
        logger.debug(s"\t\ttarget mz: ${target.accurateMass.get} - feature mz: ${spectra.accurateMass.get}")
      }

      result
    }
    else {
      false
    }
  }
}

/**
  * includes by mass range
  *
  * @param target
  * @param windowInDa window value in mDa
  */
class IncludeByMassRange(val target: Target, val windowInDa: Double) extends Filter[AccurateMassSupport] with Logging {
  logger.debug(s"mass window is ${windowInDa} for ${target}")

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  protected override def doInclude(spectra: AccurateMassSupport, applicationContext: ApplicationContext): Boolean = {
    val error = MassAccuracy.calculateMassError(spectra, target)
    logger.debug(s"mass error is: ${error} for ${spectra} and ${target}")

    if (error.isDefined) {
      val result = error.get <= windowInDa

      logger.debug(s"\t=> accepted $result")
      result
    }
    else {
      false
    }
  }
}
