package edu.ucdavis.fiehnlab.wcmc.apps.localrunner

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
import org.springframework.context.annotation.{Bean, Configuration, Profile}
import org.springframework.stereotype.Component

import scala.io.Source

object LocalRunner extends App {
  val app = new SpringApplication(classOf[LocalRunner])
  app.setWebApplicationType(WebApplicationType.NONE)
  args.foreach(println)
  val context = app.run(args: _*)
}

@Component
@Profile(Array("!test"))
class CommandLineParser @Autowired()(runner: Runner) extends CommandLineRunner with Logging {

  override def run(args: String*): Unit = {
    if (args.size != 2) {
      logger.error("\nPlease provide a sample list file and an acquisition method name.\n")
      System.exit(0)
    } else {
      logger.info(s"Arguments: ${args.mkString("\n")}")
    }

    val method = args(1)
    try {
      val fileList = Source.fromFile(args(0)).getLines().filterNot(_.isEmpty).map(line =>
        if (line.toLowerCase().endsWith(".mzml"))
          line
        else
          s"${line}.mzml"
      ).toSeq

      runner.process(fileList, AcquisitionMethod.deserialize(method))
    } catch {
      case ex: FileNotFoundException =>
        logger.error(s"File ${args(0)} not found.")
        System.exit(-1)
      case ex: Throwable => logger.error(s"Something bad happened: ${ex.getMessage}")
    }

    System.exit(0)
  }
}

@Component
class Runner() extends Logging {

  @Autowired
  val workflow: Workflow[Double] = null

  @Autowired
  val taskRunner: TaskRunner = null

  def process(fileList: Seq[String], method: AcquisitionMethod,email:Option[String] = None, organ:String = "", species:String = ""): Unit = {
    fileList.foreach { sample =>
      logger.info(s"Processing sample: ${sample}")
      val task = Task(s"${sample} processing",
        email,
        method,
        Seq(SampleToProcess(sample, "", "", sample,
          Matrix(System.currentTimeMillis().toString, species, organ, Seq.empty)
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

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class LocalRunner {
}

@Configuration
class LocalRunnerConfiguration extends Logging {
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

