package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import com.typesafe.scalalogging.LazyLogging
import org.springframework.context.annotation.Configuration
//import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, SampleLoader}
//import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
//import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
//import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.{CommandLineRunner, SpringApplication}
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class CloudRunner extends CommandLineRunner with LazyLogging {
  @Value("${carrot.sample:#{environment.CARROT_SAMPLE}}")
  val sampleName: String = null

  @Value("${carrot.method:#{environment.CARROT_METHOD}}")
  val method: String = null

  @Autowired
  val env: Environment = null

  override def run(args: String*): Unit = {

    logger.info(s"profiles: [${env.getActiveProfiles.mkString("; ")}]")


    if (sampleName.isEmpty || method == null) {
      logger.error("One or more required environment variables are not defined. Please set CARROT_SAMPLE, CARROT_METHOD with correct values.")
      System.exit(1)
    }

    logger.info(s"Sample name: ${sampleName}")
    logger.info(s"Method: ${method}")

    System.exit(0)
  }
}

@Configuration
class TestConfig {

  @Bean
  def workflow: Workflow[Double] = {
    new Workflow[Double]()
  }

}

object CloudRunner extends App {
  val app = new SpringApplication(classOf[CloudRunner])
  val context = app.run(args: _*)
}


//profiles: [carrot.lcms.correction; carrot.gcms.correction; carrot.report.quantify.height; carrot.processing.replacement.simple; carrot.processing.peakdetection; file.source.eclipse; carrot.lcms]
