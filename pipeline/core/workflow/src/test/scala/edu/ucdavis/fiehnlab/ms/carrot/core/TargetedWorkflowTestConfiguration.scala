package edu.ucdavis.fiehnlab.ms.carrot.core

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, TxtStreamLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.CentralWorkflowConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation._

/**
  * Test configuration of a LCMS target workflow
  */
@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Configuration
@Import(Array(classOf[CentralWorkflowConfig]))
class TargetedWorkflowTestConfiguration extends LazyLogging {

  @Autowired
  val resourceLoader: DelegatingResourceLoader = null

  /**
    * our defined library of library targets
    *
    * @return
    */
  @Profile(Array("backend-txt"))
  @Bean
  def targetLibrary: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](resourceLoader.loadAsFile("targets.txt").get, "\t")

}
