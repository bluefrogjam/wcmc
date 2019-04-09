package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, MergeLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import org.apache.logging.log4j.scala.Logging
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}

/**
  * Created by wohlgemuth on 9/15/17.
  */
@Configuration
@ComponentScan
class ScheduleConfig extends Logging {
  @Bean
  def workflow: Workflow[Double] = new Workflow[Double]

  @Bean
  def mergedLibrary(correction: DelegateLibraryAccess[CorrectionTarget], annotation: DelegateLibraryAccess[AnnotationTarget]): MergeLibraryAccess = new MergeLibraryAccess(correction, annotation)
}
