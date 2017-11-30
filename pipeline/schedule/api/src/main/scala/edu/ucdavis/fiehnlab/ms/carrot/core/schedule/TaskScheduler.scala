package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.Task
import edu.ucdavis.fiehnlab.utilities.email.EmailService
import org.springframework.beans.factory.annotation.{Autowired, Value}

/**
  * provides us access with scheduling a task in the system.
  */
trait TaskScheduler {

  @Value("${wcmc.pipeline.workflow.config.email.sender:binbase@gmail.com}")
  val emailSender: String = ""

  @Autowired
  val emailService:EmailService = null

  /**
    * runs this provided task
    *
    * @param task
    */
  final def submit(task: Task): String = {
    verify(task)

    //send notification email
    emailService.send(emailSender,task.email :: List(),s"Dear user, your job with ${task.name} has been submitted for calculations","job scheduled",None)

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

