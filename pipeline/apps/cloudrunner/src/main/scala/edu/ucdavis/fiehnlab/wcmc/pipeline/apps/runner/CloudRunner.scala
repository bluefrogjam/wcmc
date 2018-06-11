package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner
//import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, SampleLoader}
//import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
//import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
//import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Bean

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class CloudRunner {

  @Bean
  def workflow: Workflow[Double] = {
    new Workflow[Double]()
  }

}

object CloudRunner extends App {
  val app = new SpringApplication(classOf[CloudRunner])
  val context = app.run(args: _*)
}
