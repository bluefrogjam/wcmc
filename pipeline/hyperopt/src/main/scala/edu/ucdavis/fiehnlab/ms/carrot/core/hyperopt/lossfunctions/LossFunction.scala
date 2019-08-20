package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSample, CorrectedSample, Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.RejectDueToCorrectionFailed

abstract class LossFunction[T <: Sample] {

  /**
    * return a list of all annotated correction features grouped by compound
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
    * @param samples
    * @return
    */
  abstract def lossFunction(samples: List[T]): Double
}
