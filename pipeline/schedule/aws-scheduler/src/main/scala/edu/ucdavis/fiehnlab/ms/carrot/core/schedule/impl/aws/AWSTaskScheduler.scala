package edu.ucdavis.fiehnlab.ms.carrot.core.schedule.impl.aws

import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.Task
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.TaskScheduler
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client.StasisClient
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
  val stasisClient: StasisClient = null

  /**
    * does the actual submitting of the task to a processing engine
    *
    * @param task
    * @return
    */
  override protected def doSubmit(task: Task): String = {
    task.samples.foreach { sample =>
      stasisClient.schedule(sample.fileName, AcquisitionMethod.serialize(task.acquisitionMethod), task.mode, task.env)
    }

    s"task ${task.name} submitted"
  }
}

@Configuration
@ComponentScan
class AWSConfiguration{

}
