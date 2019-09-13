package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSample, CorrectedSample, Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.RejectDueToCorrectionFailed

abstract class LossFunction[T <: Sample] extends Serializable {

  val params: Map[String, Any] = Map()

  // set various parameters used by loss functions
  // ugly, but better to have fixed map keys
  def setMassAccuracy(massAccuracy: Double): Unit = params("massAccuracy") = massAccuracy
  def setRtAccuracy(rtAccuracy: Double): Unit = params("rtAccuracy") = rtAccuracy
  def setIntensityThreshold(intensityThreshold: Double): Unit = params("intensityThreshold") = intensityThreshold
  def setTargetCount(targetCount: Double): Unit = params("targetCount") = targetCount


  /**
    *
    * @param samples
    * @param data
    * @return
    */
  def calculateScalingByTargetCount(samples: List[T], data: Map[Target, List[Feature]], targetCount: Option[Int] = None): Double = {
    val targets: Option[Any] = targetCount orElse params.get("targetCount")

    if (targets.isDefined) {
      data.values.map(_.size).sum.toDouble / (samples.length * targets.asInstanceOf[Int])
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
