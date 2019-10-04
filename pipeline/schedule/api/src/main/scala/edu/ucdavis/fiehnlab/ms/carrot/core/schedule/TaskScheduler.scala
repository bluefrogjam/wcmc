package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.Task
import edu.ucdavis.fiehnlab.utilities.email.{EmailService, EmailServiceable}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.TrackingData
import org.springframework.beans.factory.annotation.{Autowired, Value}

/**
  * provides us access with scheduling a task in the system.
  */
trait TaskScheduler {

  @Value("${wcmc.workflow.config.email.sender:binbase@gmail.com}")
  val emailSender: String = ""

  @Autowired
  val emailService: EmailServiceable = null

  @Autowired
  val stasisCli: StasisService = null

  /**
    * runs this provided task
    *
    * @param task
    */
  final def submit(task: Task): String = {
    verify(task)

    updateTracking(task)
    //send notification email

    task.email match {
      case Some(email) =>
        emailService.send(emailSender, task.email.get :: List(), s"Dear user, your job with ${task.name} has been submitted for calculations", "job scheduled", None)
      case None =>
    }

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

  final def updateTracking(task: Task): Unit = {
    task.samples.foreach(sample => {
      val name = sample.fileName.split('.').head
      stasisCli.addTracking(TrackingData(name, "scheduled", sample.fileName))
    })
  }
}

