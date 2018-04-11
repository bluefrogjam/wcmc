package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.Filter
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.MassAccuracy
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{AccurateMassSupport, Feature}
import org.springframework.context.ApplicationContext

/**
  * Created by wohlgemuth on 7/12/17.
  */
class IncludeByMassRangePPM(val target: Target, val windowInPPM: Double, val phaseToLog: String) extends Filter[AccurateMassSupport] with LazyLogging {
  logger.debug(s"mass window is ${windowInPPM} for ${target}")

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  protected override def doInclude(spectra: AccurateMassSupport, applicationContext: ApplicationContext): Boolean = {
    val error = MassAccuracy.calculateMassErrorPPM(spectra, target)
    logger.debug(s"mass error is: ${error} for ${spectra} and ${target}")

    if (error.isDefined) {
      val result = error.get <= windowInPPM

      logger.debug(s"\t=> accepted $result")
      result
    }
    else {
      false
    }
  }

  /**
    * references to all used settings
    */
  override protected val usedSettings: Map[String, Any] = Map("target" -> target, "massAccuracyWindowInPPM" -> windowInPPM)
}

/**
  * includes by mass range
  *
  * @param target
  * @param window
  */
class IncludeByMassRange(val target: Target, val window: Double, val phaseToLog: String) extends Filter[AccurateMassSupport] with LazyLogging {
  logger.debug(s"mass window is ${window} for ${target}")

  /**
    * this returns true, if the spectra should be included, false if it should be excluded
    */
  protected override def doInclude(spectra: AccurateMassSupport, applicationContext: ApplicationContext): Boolean = {
    val error = MassAccuracy.calculateMassError(spectra, target)
    logger.debug(s"mass error is: ${error} for ${spectra} and ${target}")

    if (error.isDefined) {
      val result = error.get <= window

      logger.debug(s"\t=> accepted $result")
      result
    }
    else {
      false
    }
  }

  /**
    * references to all used settings
    */
  override protected val usedSettings: Map[String, Any] = Map("target" -> target, "massAccuracyWindowInDalton" -> window)
}
