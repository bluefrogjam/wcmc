package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{SampleToProcess, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.{TaskRunner, TaskScheduler}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class Runner extends CommandLineRunner with LazyLogging {
  @Value("${carrot.sample:#{environment.CARROT_SAMPLE}}")
  val sampleName: String = null

  @Value("${carrot.method:#{environment.CARROT_METHOD}}")
  val method: String = null

  @Autowired
  val taskRunner: TaskRunner = null

  override def run(args: String*): Unit = {


    if (sampleName.isEmpty || method == null) {
      logger.error("One or more required environment variables are not defined. Please set CARROT_SAMPLE, CARROT_METHOD with correct values.")
    }
    else {
      logger.info(s"Sample name: ${sampleName}")
      logger.info(s"Method: ${method}")
    }


    taskRunner.run(Task(
      name = s"processing ${sampleName} with ${method}",
      email = "wohlgemuth@ucdavis.edu",
      acquisitionMethod = AcquisitionMethod.deserialize(method),
      samples = Seq(SampleToProcess(sampleName))
    ))
  }
}
