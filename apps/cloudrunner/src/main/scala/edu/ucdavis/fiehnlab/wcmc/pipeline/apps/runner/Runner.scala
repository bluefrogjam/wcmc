package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{SampleToProcess, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.TaskRunner
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.CommandLineRunner
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class Runner extends CommandLineRunner with LazyLogging {
  @Value("${carrot.sample:#{environment.CARROT_SAMPLE}}")
  val sampleName: String = null

  @Value("${carrot.method:#{environment.CARROT_METHOD}}")
  val method: String = null

  @Value("${carrot.mode:#{environment.CARROT_MODE}}")
  val mode: String = null

  @Value("${carrot.submitter:wohlgemuth@ucdavis.edu}")
  val submitter: String = null

  @Autowired
  val taskRunner: TaskRunner = null

  @Autowired
  val context: ApplicationContext = null

  override def run(args: String*): Unit = {


    if (sampleName.isEmpty || method == null) {
      logger.error("One or more required environment variables are not defined. Please set CARROT_SAMPLE, CARROT_METHOD environment variables with correct values.")
    }

    taskRunner.run(Task(
      name = s"processing ${sampleName} with ${method}",
      email = submitter,
      mode = mode,
      acquisitionMethod = AcquisitionMethod.deserialize(method),
      samples = Seq(SampleToProcess(sampleName)),
      env = context.getEnvironment.getActiveProfiles.filter(p => Set("prod", "dev", "test").contains(p)).head
    ))
  }
}
