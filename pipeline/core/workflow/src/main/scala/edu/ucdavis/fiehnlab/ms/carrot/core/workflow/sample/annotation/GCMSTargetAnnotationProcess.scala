package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.AnnotateSampleProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSample, CorrectedSample, Target}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(Array("carrot.gcms"))
class GCMSTargetAnnotationProcess @Autowired()(val targets: LibraryAccess[Target], val gcmsPropterties: LCMSTargetAnnotationProperties) extends AnnotateSampleProcess(targets) with LazyLogging {

  /**
    * this implements the annotation process of the given corrected sample against the provided list of targets
    *
    * @param input
    * @return
    */
  override def process(input: CorrectedSample, targets: Iterable[Target], method: AcquisitionMethod): AnnotatedSample = ???
}
