package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

/**
  * provides us access with scheduling a task in the system.
  */
trait TaskScheduler {

  /**
    * runs this provided task
    *
    * @param task
    */
  final def submit(task: Task): String = {
    verify(task)

    doSubmit(task)
  }

  /**
    * does the actual submitting of the task to a processing engine
    *
    * @param task
    * @return
    */
  protected def doSubmit(task: Task): String

  /**
    * verifies that the task has all the required parameters
    *
    * @param task
    * @return
    */
  protected def verify(task: Task) = {
    assert(task != null, "you need to provide a task!")
    assert(task.name != null, "you need to provide a task name")
    assert(task.samples != null, "you need to provide samples!")
    assert(task.samples.nonEmpty, "you need to provide samples!")
    assert(task.email != null, "you need to provide a valid email address")
  }

}

