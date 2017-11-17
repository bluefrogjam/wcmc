package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{ResultStorage, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

/**
  * This is the basic Runner, required to execute our workflows
  * and process the data.
  *
  * The behavior can be adjusted by injected additional beans to modify the generated results.
  */
@Component
class TaskRunner extends LazyLogging {

  @Autowired
  val workflow: Workflow[Double] = null

  @Autowired
  val sampleLoader: SampleLoader = null

  /**
    * This provides the system with N-instructions after the processing is finished. The concrete implementations can write the result to a file,
    * do additional processing, store it in databases.
    */
  @Autowired(required = false)
  val storage: java.util.Collection[ResultStorage] = new util.ArrayList[ResultStorage]()

  /**
    * runs the specified TASK
    *
    * @param task
    */
  final def run(task: Task) = {

    assert(task.acquisitionMethod != null)
    assert(task.email != null)
    assert(task.name != null)

    logger.info(s"executing received task: ${task}")

    val classes: Seq[ExperimentClass] = task.samples.groupBy(_.matrix).map { entry =>
      val samples = entry._2.par.map { x =>
        assert(x.fileName != null, "you need to provide a file name!")
        assert(x.fileName.length > 0, "you need to provide a file name!")

        sampleLoader.getSample(x.fileName)
      }

      ExperimentClass(samples.seq, Option(entry._1))
    }.toSeq

    val experiment = Experiment(
      classes,
      Some(task.name),
      acquisitionMethod = task.acquisitionMethod
    )

    logger.info(s"starting to process the generated experiment: ${experiment}")
    val result = workflow.process(experiment)


    //send the processed result to the storage engine.
    storage.asScala.foreach { x: ResultStorage =>
      try {
        x.store(result, task)
      }
      catch {
        case e: Exception =>
          logger.warn(s"execption observed during storing of the workflow result: ${e.getMessage}", e)
      }
    }

    //notify people by email, if required

  }
}
