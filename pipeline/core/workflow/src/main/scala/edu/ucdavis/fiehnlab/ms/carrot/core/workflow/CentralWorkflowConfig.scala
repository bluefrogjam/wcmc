package edu.ucdavis.fiehnlab.ms.carrot.core.workflow

import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.io.{CachedSampleLoader, ResourceLoaderSampleLoader}
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

  /**
    * main loader to be utilized for performance reasons
    *
    * @param sampleLoader
    * @return
    */
  @Bean
  @Primary
  def cachedSampleLoader(sampleLoader: ResourceLoaderSampleLoader): SampleLoader = new CachedSampleLoader(sampleLoader)

  @Bean
  def remoteLoader: FServ4jClient = new FServ4jClient

}
