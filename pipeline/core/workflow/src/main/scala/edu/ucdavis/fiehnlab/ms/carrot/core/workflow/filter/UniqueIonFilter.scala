package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.filter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.diagnostics.JSONTargetLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.filter.{Filter, MassFilter}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import org.springframework.context.ApplicationContext

/**
  * filters based on the unique mass of a target
  */
class UniqueIonFilter(target: Target, override protected val phaseToLog: String,val accuracy:Double) extends MassFilter[Feature](accuracy) with JSONTargetLogging{
  /**
    * which target we require to log
    */
  override protected val targetToLog: Target = target
  /**
    * references to all used settings
    */
  override protected val usedSettings: Map[String, Any] = Map()

  /**
    * a method which should be overwritten, if the filter can provide details why it failed
    *
    * @param spectra
    * @param applicationContext
    * @return
    */
  override protected def doIncludeWithDetails(spectra: Feature, applicationContext: ApplicationContext): (Boolean, Any) = {

    spectra.uniqueMass match {

      case Some(mass) if target.uniqueMass.isDefined => (sameMass(spectra.uniqueMass.get,target.uniqueMass.get),"same mass was observed")

      case _ => (false, "tested feature has no unique mass")
    }
  }
}
