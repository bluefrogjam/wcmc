package edu.ucdavis.fiehnlab.ms.carrot.core.schedule.impl

import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.Task
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.{TaskRunner, TaskScheduler}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Component

/**
  * Created by wohlgemuth on 9/14/17.
  */
@Component
@Profile(Array("carrot.scheduler.local"))
class ThreadExecutorTaskScheduler extends TaskScheduler {

  @Autowired
  val taskExecutor: TaskExecutor = null

  @Autowired
  val taskRunner: TaskRunner = null

  /**
    * does the actual submitting of the task to a processing engine
    *
    * @param task
    * @return
    */
  override protected def doSubmit(task: Task): String = {
    taskExecutor.execute(new Runnable {
      override def run(): Unit = {
        taskRunner.run(task)
      }
    }
    )
    task.name
  }
}
