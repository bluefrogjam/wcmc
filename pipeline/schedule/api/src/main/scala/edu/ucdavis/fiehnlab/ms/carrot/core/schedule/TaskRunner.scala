package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{ResultStorage, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.exception.UnsupportedSampleException
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import edu.ucdavis.fiehnlab.utilities.email.EmailService
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

/**
  * This is the basic Runner, required to execute our workflows
  * and process the data.
  *
  * The behavior can be adjusted by injected additional beans to modify the generated results.
  */
@Component
@Profile(Array("carrot.runner.required"))
class TaskRunner extends LazyLogging {


  @Value("${wcmc.pipeline.workflow.config.email.sender:binbase@gmail.com}")
  val emailSender: String = ""

  @Autowired
  val workflow: Workflow[Double] = null

  @Autowired
  val sampleLoader: SampleLoader = null

  @Autowired
  val emailService: EmailService = null

  /**
    * This provides the system with N-instructions after the processing is finished. The concrete implementations can write the result to a file,
    * do additional processing, store it in databases.
    */
  @Autowired(required = false)
  val storage: java.util.Collection[ResultStorage] = new util.ArrayList[ResultStorage]()

  @Autowired
  val context: ApplicationContext = null

  /**
    * runs the specified TASK
    *
    * @param task
    */
  final def run(task: Task) = {

    assert(task.acquisitionMethod != null)
    assert(task.email != null)
    assert(task.name != null)
    assert(task.samples != null)
    assert(task.samples.nonEmpty)
    assert(task.mode != null, "task.mode cannot be null")
    assert(task.env != null, "task.env cannot be null")

    logger.info(s"executing received task: ${task} and discovering ${task.samples.size} files")
    val classes: Seq[ExperimentClass] = task.samples.groupBy(_.matrix).map { entry =>
      val samples = entry._2.map { x =>
        assert(x.fileName != null, "you need to provide a file name!")
        assert(x.fileName.length > 0, "you need to provide a file name!")

        try {
          //processes the actual sample
          val value = sampleLoader.loadSample(x.fileName)
          assert(value.isDefined, "please ensure that specified file name is defined!")
          workflow.process(value.get, task.acquisitionMethod)
        }
        catch {
          case e: UnsupportedSampleException =>
            logger.warn(s"discovered a none supported sample format, ignoring it: ${x.fileName}")
            null
          case e: AssertionError =>
            logger.error(s"Missing sample '${x.fileName}' data file. skipping from process")
            null
        }
      }.filter(x => x != null)

      ExperimentClass(samples.seq, Option(entry._1))
    }.toSeq

    val experiment = Experiment(
      classes,
      Some(task.name),
      acquisitionMethod = task.acquisitionMethod
    )



    //send the processed result to the storage engine.
    storage.asScala.par.foreach { x: ResultStorage =>
      try {
        x.store(experiment, task)
      }
      catch {
        case e: Exception =>
          logger.warn(s"execption observed during storing of the workflow result: ${e.getMessage}", e)
      }
    }

    //send notification email
    emailService.send(emailSender, task.email :: List(), s"Dear user, your result with ${task.name} is now ready for download!", "carrot: your result is finished", None)

  }
}
