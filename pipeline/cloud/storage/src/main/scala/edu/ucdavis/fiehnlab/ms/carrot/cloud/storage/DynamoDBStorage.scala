package edu.ucdavis.fiehnlab.ms.carrot.cloud.storage

import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{ResultStorage, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSample, Sample}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * utilizes a dynamo db table to store any of our results
  */
@Component
@Profile(Array("carrot.store.result.dynamo"))
class DynamoDBStorage[T] extends ResultStorage {

  /**
    * store the given experiment
    *
    * @param experiment
    */
  override def store(experiment: Experiment, task: Task): Unit = experiment.classes.foreach { clazz =>
    clazz.samples.foreach {
      case sample: QuantifiedSample[T] =>
        storeSample(experiment, clazz, sample, task);
    }
  }

  /**
    * stores a single sample in the database
    *
    * @param experiment
    * @param clazz
    * @param sample
    * @param task
    */
  def storeSample(experiment: Experiment, clazz: ExperimentClass, sample: QuantifiedSample[T], task: Task) = {

  }
}

