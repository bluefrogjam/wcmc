package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSample, CorrectedSample, Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.RejectDueToCorrectionFailed

abstract class LossFunction[T <: Sample] extends Serializable {

  val params: Map[String, Any] = Map()

  // set various parameters used by loss functions
  // ugly, but better to have fixed map keys
  def setMassAccuracy(massAccuracy: Double): Unit = params("massAccuracy") = massAccuracy
  def setTtAccuracy(rtAccuracy: Double): Unit = params("rtAccuracy") = rtAccuracy
  def setTargetCount(targetCount: Double): Unit = params("targetCount") = targetCount


  /**
    * return a list of all annotated correction features grouped by compound
    *
    * @param corrected
    * @return
    */
  protected def getTargetsAndAnnotationsForCorrectedSamples(corrected: List[CorrectedSample]): Map[Target, List[(Target, Feature)]] = {
    corrected.flatMap {
      item: CorrectedSample =>
        if (item.correctionFailed) {
          throw new RejectDueToCorrectionFailed
        }
        else {
          item.featuresUsedForCorrection.map {
            annotation =>
              (annotation.target, annotation.annotation)
          }
        }
    }.groupBy(_._1)
  }

  /**
    * return a list of all annotated features grouped by compound
    *
    * @param corrected
    * @return
    */
  protected def getTargetsAndAnnotationsForAnnotatedSamples(corrected: List[AnnotatedSample]): Map[Target, List[(Target, Feature)]] = {
    corrected.flatMap {
      item: AnnotatedSample => item.spectra.map(s => (s.target, s))
    }.groupBy(_._1)
  }


  /**
    * loss function to be implemented
    *
    * @param samples
    * @return
    */
  def lossFunction(samples: List[T]): Double
}
