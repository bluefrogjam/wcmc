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
    * @param targetCount total number of targets required (can be more than what was annotated)
    * @return
    */
  def peakHeightMeanRsd(samples: List[T], data: Map[Target, List[(Target, Feature)]], targetCount: Option[Int]): Double = {

    val rsd = data.map {
      item =>
        val annotations = item._2.map(_._2)

        val heights = annotations.collect {
          case feature: MSSpectra if feature.metadata.contains("peakHeight") =>
            feature.metadata("peakHeight").asInstanceOf[Some[Double]].get
        }

        val stdDev = Statistics.rsdDev(heights)
        (item._1, stdDev)
    }.collect {
      case x if !x._2.isNaN =>
        x

    }

    // ratio of annotation count to maximum number of possible annotations
    val scaling =
      if (targetCount.isDefined && targetCount.get > 0) {
        data.values.map(_.size).sum.toDouble / (samples.length * targetCount.get)
      } else {
        1
      }

    val mean = Statistics.mean(rsd.values) / scaling

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