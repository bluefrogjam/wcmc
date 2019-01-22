package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import java.io.{ByteArrayOutputStream, FileNotFoundException, PrintStream}
import java.util

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.exception.ProcessException
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{ResultStorage, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.exception.UnsupportedSampleException
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import edu.ucdavis.fiehnlab.utilities.email.EmailService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.TrackingData
import javax.mail.AuthenticationFailedException
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
class TaskRunner extends Logging {


  @Value("${wcmc.pipeline.workflow.config.email.sender:binbase@gmail.com}")
  val emailSender: String = ""

  @Autowired
  val workflow: Workflow[Double] = null

  @Autowired
  val sampleLoader: SampleLoader = null

  @Autowired
  val emailService: EmailService = null

  @Autowired
  val stasisCli: StasisService = null

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

        stasisCli.addTracking(TrackingData(x.fileName.split('.').head, "scheduled", x.fileName))

        try {
          //processes the actual sample
          val value = sampleLoader.loadSample(x.fileName)
          assert(value.isDefined, "please ensure that specified file name is defined!")
          workflow.process(value.get, task.acquisitionMethod)
        }
        catch {
          case e: UnsupportedSampleException =>
            logger.warn(s"discovered a none supported sample format, ignoring it: ${x.fileName}", e)
            null
          case e: AssertionError =>
            logger.error(s"assertion error in sample sample: ${x.fileName} data file. skipping from process", e)
            null
          case e: FileNotFoundException =>
            logger.error(s"sorry we did not find the sample: ${x.fileName}", e)
            null
          case e: ProcessException =>
            logger.error(s"The sample ${x.fileName} broke out processing", e)
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
        emailService.send(emailSender, task.email :: List(),
          s"Dear user, your result with ${task.name} is now ready for download!",
          "carrot: your result is finished",
          None)
      } catch {
        case e: AuthenticationFailedException => logger.warn(s"EmailService can't send email. ${e.getMessage}")
        case e: Exception =>
          logger.warn(s"execption observed during storing of the workflow result: ${e.getMessage}", e)
          val os = new ByteArrayOutputStream()
          e.printStackTrace(new PrintStream(os))
          emailService.send(emailSender, task.email :: List(),
            s"Dear user, the task '${task.name}' did not execute properly!\n\n${os.toString("UTF8")}",
            s"carrot: processing of ${task.name} had problems.",
            None)
      }
    }
  }
}
