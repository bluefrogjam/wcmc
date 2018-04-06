package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.QuantificationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * quantifies the data in the given sample by height
  *
  * @param properties
  */
@Component
@Profile(Array("carrot.report.quantify.height"))
class QuantifyByHeightProcess @Autowired()(libraryAccess: LibraryAccess[Target]) extends QuantificationProcess[Double](libraryAccess) {

  /**
    * computes the height by utilizing the mass from the target
    *
    * @param target
    * @param spectra
    * @return
    */
  protected override def computeValue(target: Target, spectra: Feature): Option[Double] = if (spectra.massOfDetectedFeature.isDefined) Some(spectra.massOfDetectedFeature.get.intensity) else None
}
