package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSample, CorrectedSample, Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra, MetadataSupport}
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.Statistics


abstract class PeakHeightRSDLossFunction[T <: Sample] extends LossFunction[T] {

  /**
    * calculates the average rsd in peak height by metabolite over all samples
    * @param samples sample data
    * @param data map of annotations grouped by target
    * @param targetCount total number of targets required (can be more than what was annotated)
    * @return
    */
  def peakHeightMeanRsd(samples: List[T], data: Map[Target, List[(Target, Feature)]], targetCount: Option[Int]): Double = {

    val rsd = data.map {
      item =>
        val annotations = item._2.map(_._2)

        val heights = annotations.collect {
          case feature: MSSpectra with MetadataSupport =>
            feature.metadata("peakHeight").asInstanceOf[Some[Double]].get
        }

        val stdDev = Statistics.rsdDev(heights)
        (item._1, stdDev)
    }

    // ratio of annotation count to maximum number of possible annotations
    val scaling =
      if (targetCount.isDefined && targetCount.get > 0) {
        data.values.map(_.size).sum.toDouble / (samples.length * targetCount.get)
      } else {
        1
      }

    Statistics.mean(rsd.values) / scaling
  }
}


class PeakHeightRSDCorrectionLossFunction extends PeakHeightRSDLossFunction[CorrectedSample] {

  def lossFunction(corrected: List[CorrectedSample], targetCount: Option[Int]): Double = {
    val targetsAndAnnotationsForAllSamples = getTargetsAndAnnotationsForCorrectedSamples(corrected)
    peakHeightMeanRsd(corrected, targetsAndAnnotationsForAllSamples, targetCount)
  }
}

class PeakHeightRSDAnnotationLossFunction extends PeakHeightRSDLossFunction[AnnotatedSample] {

  def lossFunction(annotated: List[AnnotatedSample], targetCount: Option[Int]): Double = {
    val targetsAndAnnotationsForAllSamples = getTargetsAndAnnotationsForAnnotatedSamples(annotated)
    peakHeightMeanRsd(annotated, targetsAndAnnotationsForAllSamples, targetCount)
  }
}