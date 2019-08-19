package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.RejectDueToCorrectionFailed

abstract class LossFunction {

  /**
    * Return a list of all annotated correction features grouped by metabolite
    * @param corrected
    * @return
    */
  protected def getTargetsAndAnnotationsForAllSamples(corrected: List[CorrectedSample]): Map[Target, List[(Target, Feature)]] = {
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


  abstract def lossFunction(samples: List[CorrectedSample]): Double
}
