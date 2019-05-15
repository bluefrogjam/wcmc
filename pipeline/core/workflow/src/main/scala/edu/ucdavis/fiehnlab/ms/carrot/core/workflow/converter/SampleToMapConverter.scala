package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.converter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{GapFilledSpectra, QuantifiedSample}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.storage.CarrotToStasisConverter
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model._

class SampleToMapConverter[T] extends SampleConverter[T, Map[String, Injection]] {
  /**
    * converts a sample to an different representation
    *
    * @param sample
    * @return
    */
  override def convert(sample: QuantifiedSample[T]): Map[String, Injection] = {
    val results = sample.quantifiedTargets.map(feature => {
      Result(CarrotToStasisConverter.asStasisTarget(feature),
        Annotation(feature.retentionIndex,
          feature.quantifiedValue.get match {
            case x: Double => x.toDouble
            case _ => 0.0
          },
          replaced = feature match {
            case f: GapFilledSpectra[T] => true
            case _ => false
          },

          feature.accurateMass.getOrElse(0.0),
          nonCorrectedRt = feature.retentionTimeInSeconds,
          massError = feature.spectra.get.massAccuracy.getOrElse(-1.0),
          massErrorPPM = feature.spectra.get.massAccuracyPPM.getOrElse(-1.0)
        )
      )
    })

    Map(sample.name -> Injection(System.currentTimeMillis().toString,
      Correction(3,
        sample.correctedWith.name,
        sample.regressionCurve.getXCalibrationData.zip(sample.regressionCurve.getYCalibrationData)
          .map(pair => Curve(pair._1, pair._2))
      ),
      results)
    )


  }
}
