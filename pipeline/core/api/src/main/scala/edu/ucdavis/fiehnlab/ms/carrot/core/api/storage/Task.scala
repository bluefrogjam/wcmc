package edu.ucdavis.fiehnlab.ms.carrot.core.api.storage

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, Matrix}


/**
  * This defines a basic task to be submitted to the carrot system
  * for processing and calculations
  */
case class Task(name: String, email: Option[String], acquisitionMethod: AcquisitionMethod, samples: Seq[SampleToProcess], mode: String = null, env: String = null)


/**
  * a basic sample which should be processed
  *
  * @param fileName
  * @param matrix
  */
case class SampleToProcess(fileName: String, className: String = "", comment: String = "", label: String = "", matrix: Matrix = Matrix("", "", "", Seq.empty))

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







