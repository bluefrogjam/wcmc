package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.QuantificationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * reports the scan for each annotated spectra, which can be used to confirm and fine tune the system
  *
  * @param properties
  */
@Component
@Profile(Array("quantify-by-scan"))
class QuantifyByScanProcess @Autowired()(libraryAccess: LibraryAccess[Target]) extends QuantificationProcess[Int](libraryAccess) {

  /**
    * computes the height by utilizing the mass from the target
    *
    * @param target
    * @param spectra
    * @return
    */
  protected override def computeValue(target: Target, spectra: Feature): Option[Int] = Some(spectra.scanNumber)
}
