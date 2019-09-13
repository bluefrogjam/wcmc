package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSample, CorrectedSample, Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.Statistics
import edu.ucdavis.fiehnlab.ms.carrot.math.SimilarityMethods

abstract class GaussianSimilarityLossFunction[T <: Sample] extends LossFunction[T] with LazyLogging {

  /**
    * calculates the variability in peak similarity
    *
    * @param samples     sample data
    * @param data        map of annotations grouped by target
    * @return
    */
  def peakSimilarityDistance(samples: List[T], data: Map[Target, List[Feature]]): Double = {

    if (!params.contains("massAccuracy") || !params.contains("rtAccuracy")) {
      logger.warn("Parameters not present for computing gaussian similarity")
      Double.MaxValue
    }

    else {
      val similarities = data.map {
        case (target, features) =>
          val meanSimilarity = Statistics.mean(
            features.map(x => {
              if (params.contains("intensityThreshold")) {
                SimilarityMethods.featureTargetSimilarity(x, target,
                  params("massAccuracy").asInstanceOf[Double], params("rtAccuracy").asInstanceOf[Double], params("intensityThreshold").asInstanceOf[Double])
              } else {
                SimilarityMethods.featureTargetSimilarity(x, target,
                  params("massAccuracy").asInstanceOf[Double], params("rtAccuracy").asInstanceOf[Double])
              }
            })
          )

          (target, meanSimilarity)
      }

      val scaling = calculateScalingByTargetCount(samples, data, Some(data.size))

      // return the product of all similarities, converted to a distance, and then scaled by target count
      1.0 / similarities.values.foldLeft(1.0)(_ * _) / scaling
    }
  }
}

class GaussianSimilarityCorrectionLossFunction extends GaussianSimilarityLossFunction[CorrectedSample] {

  def lossFunction(corrected: List[CorrectedSample]): Double = {
    val targetsAndAnnotationsForAllSamples = getTargetsAndAnnotationsForCorrectedSamples(corrected)
    peakSimilarityDistance(corrected, targetsAndAnnotationsForAllSamples)
  }
}

class GaussianSimilarityAnnotationLossFunction extends GaussianSimilarityLossFunction[AnnotatedSample] {

  def lossFunction(annotated: List[AnnotatedSample]): Double = {
    val targetsAndAnnotationsForAllSamples = getTargetsAndAnnotationsForAnnotatedSamples(annotated)
    peakSimilarityDistance(annotated, targetsAndAnnotationsForAllSamples)
  }
}