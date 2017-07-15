package edu.ucdavis.fiehnlab.ms.carrot.core.workflow

import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.io.ResourceLoaderSampleLoader
import edu.ucdavis.fiehnlab.wcms.api.rest.fserv4j.FServ4jClient
import edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j.MSDialRestProcessor
import org.springframework.context.annotation._

/**
  * Created by wohlgemuth on 7/14/17.
  */
@Configuration
@ComponentScan(basePackageClasses = Array(classOf[MSDialRestProcessor]))
@Import(Array(classOf[WorkflowConfig]))
class CentralWorkflowConfig {

  /**
    * assembles all loaders in the system and allows prioritized loading ofr the resources
    *
    * @param resourceLoader
    * @return
    */
  @Bean
  def resourceSampleLoader(resourceLoader: DelegatingResourceLoader): ResourceLoaderSampleLoader = new ResourceLoaderSampleLoader(resourceLoader)

  @Bean
  def remoteLoader: FServ4jClient = new FServ4jClient

}
