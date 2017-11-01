package edu.ucdavis.fiehnlab.ms.carrot.core

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, TxtStreamLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation._

/**
  * Test configuration of a LCMS target workflow
  */
@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Configuration
class TargetedWorkflowTestConfiguration extends LazyLogging {

  @Autowired
  val resourceLoader: DelegatingResourceLoader = null


  /**
    * below there will be all different directory loaders from the different workstations we are working on
    * smarter would be to use spring profiles
    *
    * @return
    */
  @Bean
  def resourceLoaderSrc: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("src"))

  /**
    * our defined library of library targets
    *
    * @return
    */
  @Profile(Array("backend-txt"))
  @Bean
  def targetLibrary: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](resourceLoader.loadAsFile("targets.txt").get, "\t")


  @Bean
  def client:FServ4jClient = new FServ4jClient(
    "testfserv.fiehnlab.ucdavis.edu",
    80
  )
}
