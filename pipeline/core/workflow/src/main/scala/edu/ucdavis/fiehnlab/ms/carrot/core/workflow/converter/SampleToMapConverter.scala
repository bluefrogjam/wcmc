package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.converter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{GapFilledSpectra, QuantifiedSample, QuantifiedSpectra, QuantifiedTarget}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.ZeroreplacedTarget
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model._

class SampleToMapConverter[T] extends SampleConverter[T, Map[String, Injection]] {

  val resultConverter: CarrotToStasisConverter = new CarrotToStasisConverter()

  /**
    * converts a sample to an different representation
    *
    * @param sample
    * @return
    */
  override def convert(sample: QuantifiedSample[T]): Map[String, Injection] = {

    val results: Seq[Result] = sample.quantifiedTargets.collect {
      case replacedtgt: ZeroreplacedTarget =>
        Result(resultConverter.asStasisTarget(replacedtgt),
          Annotation(replacedtgt.spectraUsedForReplacement.retentionIndex,
            replacedtgt.spectraUsedForReplacement.quantifiedValue.get,
            replaced = true,
            replacedtgt.spectraUsedForReplacement.accurateMass.get,
            replacedtgt.retentionTimeInSeconds,
            massError = Math.abs(replacedtgt.precursorMass.get - replacedtgt.spectraUsedForReplacement.accurateMass.get),
            massErrorPPM = getReplMassError(replacedtgt, Some(replacedtgt.spectraUsedForReplacement), ppm = true)
          )
        )
      case quanttgt: QuantifiedTarget[T] =>
        Result(resultConverter.asStasisTarget(quanttgt),
          Annotation(quanttgt.retentionIndex,
            quanttgt.quantifiedValue.getOrElse(0.0) match {
              case x: Double => x.toDouble
              case _ => 0.0
            },
            replaced = false,
            quanttgt.precursorMass.get,
            nonCorrectedRt = quanttgt.retentionTimeInSeconds,
            massError = getTargetMassError(quanttgt, quanttgt.spectra),
            massErrorPPM = getTargetMassError(quanttgt, quanttgt.spectra, ppm = true)
          )
        )
    }

    Map(sample.name -> Injection(System.currentTimeMillis().toString,
      Correction(3,
        sample.correctedWith.name,
        sample.regressionCurve.getXCalibrationData.zip(sample.regressionCurve.getYCalibrationData)
            .map(pair => Curve(pair._1, pair._2))
      ),
      results)
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
