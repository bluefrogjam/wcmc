package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

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
