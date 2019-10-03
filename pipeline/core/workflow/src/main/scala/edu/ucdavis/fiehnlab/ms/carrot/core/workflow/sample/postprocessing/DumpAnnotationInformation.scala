package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import edu.ucdavis.fiehnlab.loader.ResourceStorage
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.PostProcessing
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSMSSpectra
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSample, QuantifiedTarget, Sample}
import edu.ucdavis.fiehnlab.spectra.hash.core.SplashFactory
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType
import edu.ucdavis.fiehnlab.spectra.hash.core.util.SpectraUtil
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(Array("carrot.processing.dump"))
class DumpAnnotationInformation @Autowired()(storage: ResourceStorage) extends PostProcessing[Double] with Logging {

  /**
    * dumps all MSMS with target identification to the linked storage
    *
    * @param item
    * @return
    */
  override def doProcess(item: QuantifiedSample[Double], method: AcquisitionMethod, rawSample: Option[Sample]): QuantifiedSample[Double] = {
    item.quantifiedTargets foreach { x: QuantifiedTarget[Double] =>

      x.spectra match {
        case Some(msms: MSMSSpectra) =>
          val spectraString = msms.associatedScan.get.ions.map { x => s"${x.mass}:${x.intensity}" }.mkString(" ")
          val splash = SplashFactory.create().splashIt(SpectraUtil.convertStringToSpectrum(spectraString, SpectraType.MS))
          logger.info(s"${x.name} - ${msms.retentionTimeInSeconds} - ${msms.retentionTimeInMinutes} - ${msms.retentionIndex} - ${msms.accurateMass} - ${msms.precursorIon} - ${splash} - ${spectraString}")
        case _ =>
      }

    }

    item
  }

}
