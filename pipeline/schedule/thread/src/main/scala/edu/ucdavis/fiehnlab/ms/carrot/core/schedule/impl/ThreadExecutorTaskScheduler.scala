package edu.ucdavis.fiehnlab.ms.carrot.core.schedule.impl

import java.io.IOException
import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.Task
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.{TaskRunner, TaskScheduler}
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Configuration, Profile}
import org.springframework.stereotype.Component

/**
  * Created by wohlgemuth on 9/14/17.
  */
@Component
@Profile(Array("carrot.scheduler.local"))
class ThreadExecutorTaskScheduler extends TaskScheduler with Logging {

  val taskExecutor: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors())

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

  override def awaitShutdown(): Unit = {
    logger.info("shutting down the executor service")
    taskExecutor.shutdown()
    taskExecutor.awaitTermination(5000, TimeUnit.DAYS)
  }
}

@Configuration
@Profile(Array("carrot.scheduler.local"))
class ThreadExecutorTaskSchedulerAutoconfiguration {

}
