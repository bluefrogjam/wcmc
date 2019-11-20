package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import java.io.ByteArrayInputStream

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.loader.ResourceStorage
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.PostProcessing
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSMSSpectra
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSample, QuantifiedTarget, Sample}
import edu.ucdavis.fiehnlab.spectra.hash.core.SplashFactory
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType
import edu.ucdavis.fiehnlab.spectra.hash.core.util.SpectraUtil
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(Array("carrot.processing.dump","carrot.processing.dump.spectra"))
class DumpAnnotationInformation @Autowired()(@Qualifier("outputStorage") storage: ResourceStorage, objectMapper: ObjectMapper) extends PostProcessing[Double] with Logging {

  /**
    * dumps all MSMS with target identification to the linked storage
    *
    * @param item
    * @return
    */
  override def doProcess(item: QuantifiedSample[Double], method: AcquisitionMethod, rawSample: Option[Sample]): QuantifiedSample[Double] = {

    val msmsSpectra = item.quantifiedTargets map { x: QuantifiedTarget[Double] =>

      val result: Option[Map[String, Any]] = x.spectra match {
        case Some(msms: MSMSSpectra) =>
          val spectraString = msms.associatedScan.get.ions.map { x => s"${x.mass}:${x.intensity}" }.mkString(" ")
          val spectraRawString = msms.associatedScan.get.rawIons.getOrElse(Seq.empty).map({ x => s"${x.mass}:${x.intensity}" }).mkString(" ")

          val splash = SplashFactory.create().splashIt(SpectraUtil.convertStringToSpectrum(spectraString, SpectraType.MS))
          val rawSplash = SplashFactory.create().splashIt(SpectraUtil.convertStringToSpectrum(spectraRawString, SpectraType.MS))

          Some(
            Map(
              "name" -> x.name.get,
              "rt (s)" -> msms.retentionTimeInSeconds,
              "rt (m)" -> msms.retentionTimeInMinutes,
              "ri" -> msms.retentionIndex,
              "accurate mass" -> msms.accurateMass.getOrElse(0.0),
              "precursor ion" -> msms.precursorIon,
              "splash" -> splash,
              "spectra" -> spectraString,
              "raw spectra" -> spectraRawString,
              "raw splash" -> rawSplash,
              "scan number" -> msms.scanNumber
            )
          )
        case _ =>
          None
      }

      result

    }


    val result = Map("file" -> item.name, "spectra" -> msmsSpectra.filter(_.isDefined))
    val string = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result)

    storage.store(new ByteArrayInputStream(string.getBytes), s"${item.name}.msms.json")
    item
  }

}
