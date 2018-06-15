package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.CommandLineRunner
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class Runner extends CommandLineRunner with LazyLogging {
  @Value("${carrot.sample:#{environment.CARROT_SAMPLE}}")
  val sampleName: String = null

  @Value("${carrot.method:#{environment.CARROT_METHOD}}")
  val method: String = null

  @Autowired
  val env: Environment = null

  @Autowired
  val workflow: Workflow[Double] = null

  @Autowired
  val sampleLoader: SampleLoader = null

  override def run(args: String*): Unit = {

    logger.info(s"profiles: [${env.getActiveProfiles.mkString("; ")}]")

    if (sampleName.isEmpty || method == null) {
      logger.error("One or more required environment variables are not defined. Please set CARROT_SAMPLE, CARROT_METHOD with correct values.")
    }
    else {
      logger.info(s"Sample name: ${sampleName}")
      logger.info(s"Method: ${method}")
    }

    workflow.process(sampleLoader.loadSample(sampleName).get, AcquisitionMethod.deserialize(method))
  }
}
