package edu.ucdavis.fiehnlab.ms.carrot.core.api.action

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample

/**
  * defines a workflow related action
  * Created by wohlgemuth on 7/12/17.
  */
trait Action {

  /**
    * executes this action
    * @param sample
    * @param experimentClass
    * @param experiment
    */
  def run(sample: Sample,experimentClass: ExperimentClass,experiment:Experiment)
}

/**
  * actions which are supposed to run after the annotation process
  */
trait PostAction extends Action
