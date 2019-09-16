package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSample, CorrectedSample, Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.RejectDueToCorrectionFailed


abstract class LossFunction[T <: Sample] extends Serializable {

  var massAccuracy: Option[Double] = None
  var rtAccuracy: Option[Double] = None
  var intensityThreshold: Option[Double] = None
  var totalTargetCount: Option[Int] = None


  /**
    *
    * @param samples
    * @param data
    * @return
    */
  def calculateScalingByTargetCount(samples: List[T], data: Map[Target, List[Feature]], targetCount: Option[Int] = None): Double = {
    val targets: Option[Int] = targetCount orElse totalTargetCount

    if (targets.isDefined) {
      data.values.map(_.size).sum.toDouble / (samples.length * targets.get)
    } else {
      1
    }
  }


  /**
    * return a list of all annotated correction features grouped by compound
    *
    * @param corrected
    * @return
    */
  protected def getTargetsAndAnnotationsForCorrectedSamples(corrected: List[CorrectedSample]): Map[Target, List[Feature]] = {
    corrected
      .flatMap {
        item: CorrectedSample =>
          if (item.correctionFailed) {
            throw new RejectDueToCorrectionFailed
          } else {
            item.featuresUsedForCorrection.map {
              annotation =>
                (annotation.target, annotation.annotation)
            }
          }
      }
      .groupBy(_._1)
      .map { case (key, value) => (key, value.map(_._2)) }
  }

  /**
    * return a list of all annotated features grouped by compound
    *
    * @param corrected
    * @return
    */
  protected def getTargetsAndAnnotationsForAnnotatedSamples(corrected: List[AnnotatedSample]): Map[Target, List[Feature]] = {
    corrected
      .flatMap { item: AnnotatedSample => item.spectra.map(s => (s.target, s)) }
      .groupBy(_._1)
      .map { case (key, value) => (key, value.map(_._2)) }
  }


  /**
    * loss function to be implemented
    *
    * @param samples
    * @return
    */
  def lossFunction(samples: List[T]): Double
}
