package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import edu.ucdavis.fiehnlab.ms.carrot.cloud.bucket.BucketStorageConfigurationProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess, MergeLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.ResultStorage
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Bean

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class CloudRunner {

  @Autowired
  @Qualifier("resultStorage")
  val resultStorage: ResultStorage = null

  @Bean
  def annotationLibrary(@Autowired(required = false) targets: java.util.List[LibraryAccess[AnnotationTarget]]): DelegateLibraryAccess[AnnotationTarget] = {
    if (targets == null) {
      new DelegateLibraryAccess[AnnotationTarget](new java.util.ArrayList())
    }
    else {
      new DelegateLibraryAccess[AnnotationTarget](targets)
    }
  }

  @Bean
  def correctionLibrary(targets: java.util.List[LibraryAccess[CorrectionTarget]]): DelegateLibraryAccess[CorrectionTarget] = new DelegateLibraryAccess[CorrectionTarget](targets)

  @Bean
  def mergedLibrary(correction: DelegateLibraryAccess[CorrectionTarget], annotation: DelegateLibraryAccess[AnnotationTarget]): MergeLibraryAccess = new MergeLibraryAccess(correction, annotation)

  @Bean
  def bucketProps: BucketStorageConfigurationProperties = new BucketStorageConfigurationProperties
}

object CloudRunner extends App {
  val app = new SpringApplication(classOf[CloudRunner])
  val context = app.run(args: _*)
  System.exit(SpringApplication.exit(context))
}
