package edu.ucdavis.fiehnlab.wcmc.apps.localrunner

import java.io.FileNotFoundException

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess, MergeLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget}
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.ScheduleConfig
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot._
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}

import scala.io.Source

object LocalRunner extends App {
  val app = new SpringApplication(classOf[LocalRunner])
  app.setWebApplicationType(WebApplicationType.NONE)
  val context = app.run(args: _*)
}

@SpringBootApplication(exclude = Array(classOf[ScheduleConfig]))
class LocalRunner extends CommandLineRunner with LazyLogging {
  @Autowired
  val workflow: Workflow[Double] = null

  override def run(args: String*): Unit = {
    if (args.size != 2) {
      logger.error("Please provide a sample list file and an acquisition method name.")
      System.exit(0)
    }

    val method = args(1)
    try {
      val fileList = Source.fromFile(args(0)).getLines().filterNot(_.isEmpty).map(line =>
        if (line.toLowerCase().endsWith(".mzml"))
          line
        else
          s"${line}.mzml"
      ).toSeq

      process(fileList, method)
    } catch {
      case ex: FileNotFoundException =>
        logger.error(s"File ${args(0)} not found.")
        System.exit(-1)
      case ex: Throwable => logger.error(s"Somethig bad happened: ${ex.getMessage}")
    }
  }

  def process(fileList: Seq[String], method: String): Unit = {
    fileList.foreach { sample =>
      logger.info(s"Processing sample: ${sample}")
      //      val task = Task(s"${sample} processing",
      //        "linuxmant@gmail.com",
      //        AcquisitionMethod(
      //          ChromatographicMethod("jenny-tribe", Some("6530"), Some("test"), Some(PositiveMode()))
      //        ),
      //        Seq(SampleToProcess(sample, "", "", sample,
      //          Matrix(System.currentTimeMillis().toString, "human", "plasma", Seq.empty)
      //        )),
      //        mode = "lcms",
      //        env = "prod"
      //      )
      //
      //      try {
      //        val start = System.currentTimeMillis()
      //        taskRunner.run(task)
      //        println()
      //        logger.info(s"\tSuccessfully finished processing ${sample} in ${(System.currentTimeMillis() - start) / 1000} s")
      //        println()
      //
      //      } catch {
      //        case ex: Exception =>
      //          logger.error(s"\tFailed processing ${sample}.", ex)
      //      }
    }
  }
}

@Configuration
@ComponentScan
class LocalRunnerConfiguration extends LazyLogging {
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
