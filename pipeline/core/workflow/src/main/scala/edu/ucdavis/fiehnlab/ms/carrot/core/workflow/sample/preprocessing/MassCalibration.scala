package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSMSSpectra}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
  * calibrates all the masses in the given samples
  * against a simple curve to improve mass accuracy.
  *
  * This is done by just adding/substracting the delta vs the reference mass for each of the configured pairs
  */
@Component
@Profile(Array("carrot.processing.calibration.simple"))
class SimpleMassCalibration extends PreProcessor with LazyLogging{
  /**
    * actually processes the item (implementations in subclasses)
    *
    * @param item
    * @return
    */
  override def doProcess(item: Sample, method: AcquisitionMethod):Sample = {

    item.spectra.foreach {

//      case msms: MSMSSpectra =>
//      case ms: MSMSSpectra =>
      case f: Feature =>
        logger.info(f.toString)
    }

    item
  }
}
