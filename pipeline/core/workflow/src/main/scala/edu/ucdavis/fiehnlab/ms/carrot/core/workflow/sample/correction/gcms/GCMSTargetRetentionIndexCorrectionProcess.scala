package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSample, Sample, Target}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(Array("carrot.gcms"))
class GCMSTargetRetentionIndexCorrectionProcess @Autowired()(libraryAccess: LibraryAccess[Target], val config:GCMSLibraryProperties) extends CorrectionProcess(libraryAccess) with LazyLogging {
  /**
    * subclasses need to overwrite this method with the exact wished behavior
    *
    * @param input
    * @return
    */
  override def process(input: Sample, targets: Iterable[Target]): CorrectedSample = ???
}
