package edu.ucdavis.fiehnlab.ms.carrot.core.schedule.impl.aws

import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.Task
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.TaskScheduler
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client.StasisService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.{ComponentScan, Configuration, Profile}
import org.springframework.stereotype.Component

/**
  * submits a task to be run on AWS fargate
  */
@Component
@Profile(Array("carrot.scheduler.aws"))
class AWSTaskScheduler extends TaskScheduler {
  @Autowired
  val context: ApplicationContext = null

  @Autowired
  val stasisClient: StasisService = null

  /**
    * does the actual submitting of the task to a processing engine
    *
    * @param task
    * @return
    */
  override protected def doSubmit(task: Task): String = {
    assert(task.mode != null, "Please provide the chromatography mode ['lcms' or 'gcms']")
    assert(task.env != null, "Please provide the running profile ['prod', 'dev' or 'test']")

    val mode = task.mode match {
      case "lcms" => "carrot.lcms"
      case "gcms" => "carrot.gcms"
      case _ => task.mode
    }

    task.samples.foreach { sample =>
      if (task.mode.startsWith("carrot."))
        stasisClient.schedule(sample.fileName, AcquisitionMethod.serialize(task.acquisitionMethod), task.mode, task.env)
      else
        stasisClient.schedule(sample.fileName, AcquisitionMethod.serialize(task.acquisitionMethod), s"carrot.${task.mode}", task.env)
    }

    s"task ${task.name} submitted"
  }
}

@Configuration
@ComponentScan
class AWSConfiguration {

}
