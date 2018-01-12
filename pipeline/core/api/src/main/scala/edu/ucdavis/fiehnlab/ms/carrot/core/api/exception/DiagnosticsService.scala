package edu.ucdavis.fiehnlab.ms.carrot.core.api.exception

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature

/**
  * a simplistic diagnostic service to allow for easy tracking of annotations accross the sytem
  */
trait DiagnosticsService {

  /**
    * diagnoses the annoation process for a feature against a given target
    * @param target the target
    * @param feature the feature
    * @param step the name of the current step
    * @param description the description of the current step
    * @param pass did this pass or fail
    */
  def diagnoseAnnotation(target:Target, feature:Feature,step:String, description:String, pass:Boolean)
}
