package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.converter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{GapFilledSpectra, QuantifiedSample, QuantifiedSpectra, QuantifiedTarget}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.ZeroreplacedTarget
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.{Ion => StIon, _}
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

@Component
@Profile(Array("carrot.output.storage.converter.sample"))
class SampleToMapConverter[T] extends SampleConverter[T, ResultData] with Logging {

  @Autowired
  val targetConverter: CarrotToStasisConverter = null

  /**
    * converts a sample to an different representation
    *
    * @param sample
    * @return
    */
  override def convert(sample: QuantifiedSample[T]): ResultData = {
    logger.info(s"converting sample ${sample.name} to ResultData")
    val results: Seq[Result] = sample.quantifiedTargets.collect {
      case replacedtgt: ZeroreplacedTarget =>

        val _ion: Option[StIon] = replacedtgt.spectraUsedForReplacement.massOfDetectedFeature match {
          case Some(p) => Some(StIon(mass = p.mass, intensity = p.intensity))
          case None => None
        }

        Result(targetConverter.asStasisTarget(replacedtgt),
          Annotation(retentionIndex = replacedtgt.spectraUsedForReplacement.retentionIndex,
            intensity = replacedtgt.spectraUsedForReplacement.quantifiedValue.get,
            replaced = true,
            mass = replacedtgt.spectraUsedForReplacement.accurateMass.get,
            msms = replacedtgt.spectraUsedForReplacement.associatedScan.getOrElse(None) match {
              case None => ""
              case t: SpectrumProperties => t.spectraString()
            },
            precursor = _ion,
            nonCorrectedRt = replacedtgt.retentionTimeInSeconds,
            massError = Math.abs(replacedtgt.precursorMass.get - replacedtgt.spectraUsedForReplacement.accurateMass.get),
            massErrorPPM = getReplMassError(replacedtgt, Some(replacedtgt.spectraUsedForReplacement), ppm = true)
          )
        )
      case quanttgt: QuantifiedTarget[T] =>

        val _ion: Option[StIon] = quanttgt.spectra match {
          case Some(p) =>
            p.massOfDetectedFeature match {
              case Some(f) => Some(StIon(mass = f.mass, intensity = f.intensity))
              case None => None
            }
          case None => None
        }

        Result(targetConverter.asStasisTarget(quanttgt),
          Annotation(retentionIndex = quanttgt.retentionIndex,
            intensity = quanttgt.quantifiedValue.getOrElse(0.0) match {
              case x: Double => x.toDouble
              case _ => 0.0
            },
            replaced = false,
            mass = quanttgt.precursorMass.get,
            msms = quanttgt.spectrum.getOrElse(None) match {
              case None => ""
              case t: SpectrumProperties => t.spectraString()
            },
            precursor = _ion,
            nonCorrectedRt = quanttgt.retentionTimeInSeconds,
            massError = getTargetMassError(quanttgt, quanttgt.spectra),
            massErrorPPM = getTargetMassError(quanttgt, quanttgt.spectra, ppm = true)
          )
        )
    }

    ResultData(sample.name, Map(sample.name ->
      Injection(System.currentTimeMillis().toString,
        Correction(3,
          sample.correctedWith.name,
          sample.regressionCurve.getXCalibrationData.zip(sample.regressionCurve.getYCalibrationData)
            .map(pair => Curve(pair._1, pair._2))
        ),
        results)
    ).asJava,
      Map.empty
    )
  }

  def getTargetMassError(target: QuantifiedTarget[T], spectrum: Option[QuantifiedSpectra[T]], ppm: Boolean = false): Double = {
    if (spectrum.isDefined)
      if (ppm)
        spectrum.get.massAccuracyPPM.getOrElse(0.0)
      else
        spectrum.get.massAccuracy.getOrElse(0.0)
    else
      -0.2
  }

  def getReplMassError(target: ZeroreplacedTarget, spectrum: Option[GapFilledSpectra[Double]], ppm: Boolean = false): Double = {
    if (spectrum.isDefined)
      if (ppm)
        spectrum.get.massAccuracyPPM.getOrElse(0.0)
      else
        spectrum.get.massAccuracy.get
    else
      -0.1
  }
}
