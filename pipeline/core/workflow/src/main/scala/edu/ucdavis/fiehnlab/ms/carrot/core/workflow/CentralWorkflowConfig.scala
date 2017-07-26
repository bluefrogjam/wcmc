package edu.ucdavis.fiehnlab.ms.carrot.core.workflow

import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.io.ResourceLoaderSampleLoader
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j
import edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j.CachedMSDialRestProcesser
import edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j.{CachedMSDialRestProcesser, MSDialRestProcessor}
import org.springframework.context.annotation._

/**
  * Created by wohlgemuth on 7/14/17.
  */
@Configuration
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

  /**
    * where to find remote files
    * @return
    */
  @Bean
  def remoteLoader: FServ4jClient = new FServ4jClient

  /**
    * which msdial rest processor to use to convert abf samples
    * @return
    */
  @Bean
  def msdialRest:msdialrest4j.MSDialRestProcessor = new CachedMSDialRestProcesser

}
