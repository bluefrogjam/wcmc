package edu.ucdavis.fiehnlab.wcmc.schedule.api

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, Matrix}
import org.springframework.beans.factory.annotation.Autowired

/**
  * This defines a basic task to be submitted to the carrot system
  * for processing and calculations
  */
case class Task(

                 name: String,

                 email: String,

                 /**
                   * the exact acquisition method we would like to use
                   * with this task
                   */
                 acquisitionMethod: AcquisitionMethod,

                 /**
                   * defines a list of samples to process
                   */
                 samples: Seq[SampleToProcess]

               )

/**
  * a basic sample which should be processed
  *
  * @param fileName
  * @param matrix
  */
case class SampleToProcess(fileName: String, matrix: Option[Matrix])

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

/**
  * provides us access with scheduling a task in the system
  */
trait TaskScheduler {

  /**
    * the implementation should be utilizing this bean to store the result
    * at a convinient location for later access
    * identified by the task id
    */
  @Autowired
  val storage: ResultStorage = null

  /**
    * runs this provided task
    *
    * @param task
    */
  def submit(task: Task): String

}

/**
  * a task scheduler which offers some more functionality
  * if you require this
  */
trait AdvancedTaskScheduler extends TaskScheduler {

  /**
    * the task has finished
    *
    * @param id
    * @return
    */
  def isFinished(id: String): Boolean

  /**
    * the task has failed
    *
    * @param id
    * @return
    */
  def isFailed(id: String): Boolean

  /**
    * the task has been scheduled
    *
    * @param id
    * @return
    */
  def isScheduled(id: String): Boolean

  /**
    * the task is currently running
    *
    * @param id
    * @return
    */
  def isRunning(id: String): Boolean

  /**
    * returns the current queue of the scheduler
    *
    * @return
    */
  def queue: Seq[String]

}
