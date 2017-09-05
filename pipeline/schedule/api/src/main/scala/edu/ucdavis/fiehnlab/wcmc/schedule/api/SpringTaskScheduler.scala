package edu.ucdavis.fiehnlab.wcmc.schedule.api

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Configuration, Profile}
import org.springframework.scheduling.annotation.{Async, EnableAsync}
import org.springframework.stereotype.Component

/**
  * Provides a simple scheduler, which runs the processing in the background of the current JVM
  *
  */
@Component
@Profile(Array("carrot.scheduler.spring"))
class SpringTaskScheduler extends TaskScheduler with LazyLogging {

  @Autowired
  val workflow: Workflow[Double] = null

  @Autowired
  val sampleLoader: SampleLoader = null

  /**
    * runs this provided task
    *
    * @param task
    */
  override def submit(task: Task): String = {
    assert(task != null,"you need to provide a task!")
    assert(task.name != null,"you need to provide a task name")
    assert(task.samples != null,"you need to provide samples!")
    assert(task.samples.nonEmpty,"you need to provide samples!")

    executeTask(task)
    task.name
  }

  @Async
  def executeTask(task: Task) : Unit= {

    logger.info(s"executing received task: ${task}")

    val classes: Seq[ExperimentClass] = task.samples.groupBy(_.matrix).map { entry =>
      val samples = entry._2.map { x =>
        assert(x.fileName != null,"you need to provide a file name!")
        assert(x.fileName.length > 0, "you need to provide a file name!")

        sampleLoader.getSample(x.fileName)
      }

      ExperimentClass(samples, entry._1)
    }.toSeq

    val experiment = Experiment(
      classes,
      Some(task.name)
    )

    logger.info(s"starting to process the generated experiment: ${experiment}")
    workflow.process(experiment)
  }
}


@EnableAsync
@Configuration
@Profile(Array("carrot.scheduler.spring"))
class SpringTaskSchedulerConfiguration {

}

