package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSample, CorrectedSample, Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.Statistics


abstract class PeakHeightRSDLossFunction[T <: Sample] extends LossFunction[T] {

  /**
    * calculates the average rsd in peak height by metabolite over all samples
    *
    * @param samples     sample data
    * @param data        map of annotations grouped by target
    * @return
    */
  def peakHeightMeanRsd(samples: List[T], data: Map[Target, List[Feature]]): Double = {

    val rsd = data
      .map {
        case (target, features) =>
          val heights: List[Double] = features.collect {
            case feature: MSSpectra if feature.metadata.contains("peakHeight") =>
              feature.metadata("peakHeight").asInstanceOf[Option[Double]].get
          }

          (target, heights)
      }
      .filter { case (target, heights) => heights.length > 1 }
      .map { case (target, heights) => (target, Statistics.rsdDev(heights)) }

    // ratio of annotation count to maximum number of possible annotations
    val scaling = calculateScalingByTargetCount(samples, data, Some(data.size))
    val mean = Statistics.mean(rsd.values) / scaling / (rsd.size.toDouble / data.size)

    mean
  }
}


class PeakHeightRSDCorrectionLossFunction extends PeakHeightRSDLossFunction[CorrectedSample] {

  def lossFunction(corrected: List[CorrectedSample]): Double = {
    val targetsAndAnnotationsForAllSamples = getTargetsAndAnnotationsForCorrectedSamples(corrected)
    peakHeightMeanRsd(corrected, targetsAndAnnotationsForAllSamples)
  }
}

class PeakHeightRSDAnnotationLossFunction extends PeakHeightRSDLossFunction[AnnotatedSample] {

  def lossFunction(annotated: List[AnnotatedSample]): Double = {
    val targetsAndAnnotationsForAllSamples = getTargetsAndAnnotationsForAnnotatedSamples(annotated)
    peakHeightMeanRsd(annotated, targetsAndAnnotationsForAllSamples)
  }
}