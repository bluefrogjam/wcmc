package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment

/**
  * provides a simple interface to store results somewhere
  * once the computation has finished
  */
trait ResultStorage {
  /**
    * store the given experiment
    *
    * @param experiment
    */
  def store(experiment: Experiment, task: Task)
}
