package edu.ucdavis.fiehnlab.wcmc.apps.singlerunner

import java.io.FileNotFoundException

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess, MergeLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{SampleToProcess, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, Matrix}
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.TaskRunner
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot._
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.{Bean, Configuration}

import scala.io.Source

object SingleRunner extends App {
  val app = new SpringApplication(classOf[SingleRunner])
  app.setWebApplicationType(WebApplicationType.NONE)
  args.foreach(println)
  val context = app.run(args: _*)
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class SingleRunner extends CommandLineRunner with Logging {
  @Autowired
  val workflow: Workflow[Double] = null

  @Autowired
  val taskRunner: TaskRunner = null

  override def run(args: String*): Unit = {
    if (args.size != 2) {
      logger.error("\nPlease provide a sample file to process and an acquisition method name.\n")
      System.exit(0)
    } else {
      logger.info(s"Arguments: ${args.mkString("\n")}")
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
      val task = Task(s"${sample} processing",
        "dpedrosa@ucdavis.edu",
        method,
        Seq(SampleToProcess(sample, "", "", sample,
          Matrix(System.currentTimeMillis().toString, "human", "plasma", Seq.empty)
        )),
        mode = "lcms",
        env = "prod"
      )
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

@Configuration
class SingleRunnerConfiguration extends Logging {
  @Bean
  def workflow: Workflow[Double] = new Workflow[Double]()

  @Bean
  def annotationLibrary(@Autowired(required = false) targets: java.util.List[LibraryAccess[AnnotationTarget]]): DelegateLibraryAccess[AnnotationTarget] = {
    if (targets == null) {
      logger.warn("no library provided, annotations will be empty!")
      new DelegateLibraryAccess[AnnotationTarget](new java.util.ArrayList())
    }
    else {
      new DelegateLibraryAccess[AnnotationTarget](targets)
    }
  }

  @Bean
  def correctionLibrary(targets: java.util.List[LibraryAccess[CorrectionTarget]]): DelegateLibraryAccess[CorrectionTarget] = new DelegateLibraryAccess[CorrectionTarget](targets)

  @Bean
  def mergedLibrary(correction: DelegateLibraryAccess[CorrectionTarget], annotation: DelegateLibraryAccess[AnnotationTarget]): MergeLibraryAccess = new MergeLibraryAccess(correction, annotation)
}
