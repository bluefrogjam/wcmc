package edu.ucdavis.fiehnlab.ms.carrot.core.workflow

import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.acquisition.AcquisitionLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.io.ResourceLoaderSampleLoader
import org.springframework.context.annotation._

/**
  * Created by wohlgemuth on 7/14/17.
  */
@Configuration
@Import(Array(classOf[WorkflowConfig]))
@ComponentScan(basePackageClasses = Array(classOf[DelegatingResourceLoader],classOf[AcquisitionLoader]))
class CentralWorkflowConfig {

  /**
    * assembles all loaders in the system and allows prioritized loading ofr the resources
    *
    * @param resourceLoader
    * @return
    */
  @Bean
  def resourceSampleLoader(resourceLoader: DelegatingResourceLoader): ResourceLoaderSampleLoader = new ResourceLoaderSampleLoader(resourceLoader)

}
