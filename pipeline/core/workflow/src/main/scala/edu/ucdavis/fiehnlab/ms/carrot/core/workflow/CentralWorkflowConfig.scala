package edu.ucdavis.fiehnlab.ms.carrot.core.workflow

import java.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.io.ConversionAwareSampleLoader
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget, Target}
import org.springframework.beans.factory.annotation.Autowired

/**
  * Created by wohlgemuth on 7/14/17.
  */
@Configuration
@ComponentScan
@EnableConfigurationProperties
@EnableCaching
class CentralWorkflowConfig extends LazyLogging{

  /**
    * assembles all loaders in the system and allows prioritized loading ofr the resources
    *
    * @param resourceLoader
    * @return
    */
  @Bean
  def loader(resourceLoader: DelegatingResourceLoader, dataFormerClient: DataFormerClient): ConversionAwareSampleLoader = new ConversionAwareSampleLoader(dataFormerClient, resourceLoader)

  /**
    * combine all annotation libraries to 1
    *
    * @param targets
    * @return
    */
  @Bean
  def annotationLibrary(@Autowired(required = false) targets: java.util.List[LibraryAccess[AnnotationTarget]]): DelegateLibraryAccess[AnnotationTarget] = {
    if (targets == null) {
      logger.warn("no library provided, annotations will be empty!")
      new DelegateLibraryAccess[AnnotationTarget](new util.ArrayList())
    }
    else {
      new DelegateLibraryAccess[AnnotationTarget](targets)
    }
  }

  /**
    * combine all correction libraries to 1
    *
    * @param targets
    * @return
    */
  @Bean
  def correctionLibrary(targets: java.util.List[LibraryAccess[CorrectionTarget]]): DelegateLibraryAccess[CorrectionTarget] = new DelegateLibraryAccess[CorrectionTarget](targets)

  /**
    * main bean providing all the libraries for methods to work on
    *
    * @param correction
    * @param annotation
    * @return
    */
  @Bean
  def mergedLibrary(correction: DelegateLibraryAccess[CorrectionTarget], annotation: DelegateLibraryAccess[AnnotationTarget]): MergeLibraryAccess = new MergeLibraryAccess(correction, annotation)

}
