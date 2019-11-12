package edu.ucdavis.fiehnlab.wcmc.apps.singlerunner

import java.io.FileNotFoundException

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess, MergeLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{SampleToProcess, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, Matrix}
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.TaskRunner
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import edu.ucdavis.fiehnlab.wcmc.apps.singlerunner.util.SpringProperties
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot._
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.core.env.ConfigurableEnvironment

import scala.collection.JavaConverters._

object SingleRunner extends App {
  val app = new SpringApplication(classOf[SingleRunner])
  app.setWebApplicationType(WebApplicationType.NONE)
  args.foreach(println)
  val context = app.run(args: _*)
}

@EnableConfigurationProperties
@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class SingleRunner extends CommandLineRunner with Logging {
  @Autowired
  val workflow: Workflow[Double] = null

  @Autowired
  val taskRunner: TaskRunner = null

  @Autowired
  val environment: ConfigurableEnvironment = null

  override def run(args: String*): Unit = {
    if (args.size != 2) {
      logger.error("\nPlease provide a sample file to process and an acquisition method name.\n")


      System.exit(0)
    } else {
      logger.info(s"Arguments: ${args.mkString("\n")}")
    }

    val properties = SpringProperties.getPropertiesStartingWith(environment, "wcmc")

    properties.asScala.toSeq.sortBy(_._1).collect {

      case (key, value) =>

        if (!key.contains("target")) {
          logger.info(s"config key: ${key} - ${value}")
        }


    }
    val method = args(1)
    try {
      process(List(args(0)), AcquisitionMethod.deserialize(method))
    } catch {
      case ex: FileNotFoundException =>
        logger.error(s"File ${args(0)} not found.")
        System.exit(-1)
      case ex: Throwable => logger.error(s"Somethig bad happened: ${ex.getMessage}")
    }

    System.exit(0)
  }

  def process(fileList: Seq[String], method: AcquisitionMethod): Unit = {
    fileList.foreach { sample =>
      logger.info(s"Processing sample: ${sample}")
      val task = Task(s"${sample} processing", None, method, Seq(SampleToProcess(sample, "", "", sample,
        Matrix(System.currentTimeMillis().toString, "human", "plasma", Seq.empty)
      )), mode = "lcms", env = "prod")
      try {
        val start = System.currentTimeMillis()
        taskRunner.run(task)
        logger.info(s"\n\tSuccessfully finished processing ${sample} in ${(System.currentTimeMillis() - start) / 1000} s\n")

      } catch {
        case ex: Exception =>
          logger.error(s"\tFailed processing ${sample}.", ex)
      }
    }


  }

}
