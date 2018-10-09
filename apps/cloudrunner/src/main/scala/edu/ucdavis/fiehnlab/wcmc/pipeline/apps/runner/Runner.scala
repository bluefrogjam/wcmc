package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{SampleToProcess, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, Matrix}
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

  @Value("#{environment.CARROT_MODE}")
  // This turns into the active profile to run the sample ['carrot.lcms','carrot.gcms']
  val mode: String = null

  @Value("${carrot.submitter:dpedrosa@ucdavis.edu}")
  val submitter: String = null

  @Autowired
  val taskRunner: TaskRunner = null

  @Autowired
  val context: ApplicationContext = null

  override def run(args: String*): Unit = {


    if (sampleName.isEmpty || method.isEmpty || mode.isEmpty) {
      logger.error("One or more required environment variables are not defined. Please set CARROT_SAMPLE, CARROT_METHOD and CARROT_MODE environment variables with correct values.")
    }

    taskRunner.run(Task(
      name = s"processing ${sampleName} with ${method}",
      email = submitter,
      acquisitionMethod = AcquisitionMethod.deserialize(method),
      samples = Seq(SampleToProcess(fileName = sampleName, matrix = Matrix("hp0", "human", "plasma", Seq.empty))),
      mode = mode,
      env = context.getEnvironment.getActiveProfiles.filter(p => Set("prod", "dev", "test").contains(p)).head
    ))
  }
}
