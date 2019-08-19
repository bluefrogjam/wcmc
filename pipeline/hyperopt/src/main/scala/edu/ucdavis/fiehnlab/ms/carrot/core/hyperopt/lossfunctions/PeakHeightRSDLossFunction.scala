package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.CorrectedSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{MSSpectra, MetadataSupport}
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.Statistics


class PeakHeightRSDLossFunction extends LossFunction {

  /**
    * calculates the average rsd in peak height by metabolite over all samples
    * @param corrected
    * @return
    */
  def lossFunction(corrected: List[CorrectedSample]): Double = {
    val targetsAndAnnotationsForAllSamples = getTargetsAndAnnotationsForAllSamples(corrected)

    val rsd = targetsAndAnnotationsForAllSamples.map {
      item =>
        val annotations = item._2.map(_._2)

        val heights = annotations.collect {
          case feature: MSSpectra with MetadataSupport =>
            feature.metadata("peakHeight").asInstanceOf[Some[Double]].get
        }

        val stdDev = Statistics.rsdDev(heights)
        (item._1, stdDev)
    }

    val averageRst = Statistics.mean(rsd.values)

    averageRst
  }
}
