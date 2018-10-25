package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, MergeLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}

/**
  * Created by wohlgemuth on 9/15/17.
  */
@Configuration
@ComponentScan
class ScheduleConfig extends LazyLogging {
  @Bean
  def workflow: Workflow[Double] = new Workflow[Double]

  @Bean
  def mergedLibrary(correction: DelegateLibraryAccess[CorrectionTarget], annotation: DelegateLibraryAccess[AnnotationTarget]): MergeLibraryAccess = new MergeLibraryAccess(correction, annotation)
}
