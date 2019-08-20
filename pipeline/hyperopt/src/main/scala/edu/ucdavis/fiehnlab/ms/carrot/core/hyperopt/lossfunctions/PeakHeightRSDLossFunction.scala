package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSample, CorrectedSample, Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra, MetadataSupport}
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.Statistics


abstract class PeakHeightRSDLossFunction[T <: Sample] extends LossFunction[T] {

  /**
    * calculates the average rsd in peak height by metabolite over all samples
    * @param data
    * @return
    */
  def peakHeightMeanRsd(data: Map[Target, List[(Target, Feature)]]): Double = {

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

    Statistics.mean(rsd.values)
  }
}


class PeakHeightRSDCorrectionLossFunction extends PeakHeightRSDLossFunction[CorrectedSample] {

  def lossFunction(corrected: List[CorrectedSample]): Double = {
    val targetsAndAnnotationsForAllSamples = getTargetsAndAnnotationsForCorrectedSamples(corrected)
    peakHeightMeanRsd(targetsAndAnnotationsForAllSamples)
  }
}

class PeakHeightRSDAnnotationLossFunction extends PeakHeightRSDLossFunction[AnnotatedSample] {

  def lossFunction(annotated: List[AnnotatedSample]): Double = {
    val targetsAndAnnotationsForAllSamples = getTargetsAndAnnotationsForAnnotatedSamples(annotated)
    peakHeightMeanRsd(targetsAndAnnotationsForAllSamples)
  }
}