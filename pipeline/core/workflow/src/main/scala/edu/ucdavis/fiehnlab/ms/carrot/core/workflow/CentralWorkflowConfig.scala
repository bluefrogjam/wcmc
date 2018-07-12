package edu.ucdavis.fiehnlab.ms.carrot.core.workflow

import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.io.ConversionAwareSampleLoader
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget, Target}

/**
  * Created by wohlgemuth on 7/14/17.
  */
@Configuration
@ComponentScan
@EnableConfigurationProperties
@EnableCaching
class CentralWorkflowConfig {

  /**
    * assembles all loaders in the system and allows prioritized loading ofr the resources
    *
    * @param resourceLoader
    * @return
    */
  @Bean
  def loader(resourceLoader: DelegatingResourceLoader, dataFormerClient: DataFormerClient): ConversionAwareSampleLoader = new ConversionAwareSampleLoader(dataFormerClient, resourceLoader)

  @Bean
  def annotation_library(targets: java.util.List[LibraryAccess[AnnotationTarget]]): DelegateLibraryAccess[AnnotationTarget] = new DelegateLibraryAccess[AnnotationTarget](targets)

  @Bean
  def correction_library(targets: java.util.List[LibraryAccess[CorrectionTarget]]): DelegateLibraryAccess[CorrectionTarget] = new DelegateLibraryAccess[CorrectionTarget](targets)


}
