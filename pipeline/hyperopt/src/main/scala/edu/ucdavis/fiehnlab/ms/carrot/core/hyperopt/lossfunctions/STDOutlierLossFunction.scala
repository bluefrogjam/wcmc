package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSample, CorrectedSample, Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{CorrectedSpectra, Feature, MSSpectra}
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.Statistics

abstract class STDOutlierLossFunction[T <: Sample] extends LossFunction[T] {

  /**
    * calculates an error value based on the presence of outliers in mass, retention time or peak height
    *
    * @param samples       sample data
    * @param data          map of annotations grouped by target
    * @param usePeakHeight optionally include peak height in addition to ri and m/z the error calculation
    *                      note that this is useful for internal standards which should have more consistent
    *                      intensities, whereas metabolites may have real biological variation
    * @return
    */
  def stdOutlierRsd(samples: List[T], data: Map[Target, List[Feature]], usePeakHeight: Boolean = true): Double = {

    // for each metabolite, calculate the ratio of the rsd of all annotations by the rsd of
    // the outlier filtered annotations.  high values indicate larger spread of the data before
    // outlier removal and therefore the presence of mis-annotations.  a ratio of 1 indicates
    // no outliers and therefore a reasonable confidence of good annotations
    val rsd = data.map {
      case (target, features) =>

        val peakHeights: List[Double] = features.collect {
          case feature: MSSpectra if feature.metadata.contains("peakHeight") =>
            feature.metadata("peakHeight").asInstanceOf[Option[Double]].get
        }

        val retentionTimes: List[Double] = features.collect {
          case feature: CorrectedSpectra =>
            feature.retentionIndex
        }

        val accurateMasses: List[Double] = features.collect {
          case feature: MSSpectra if feature.metadata.contains("ms1AccurateMass") =>
            feature.metadata("ms1AccurateMass").asInstanceOf[Option[Double]].get
        }

        // create list of properties used for error calculation
        val properties =
          if (usePeakHeight) {
            List(accurateMasses, retentionTimes)
          } else {
            List(accurateMasses, retentionTimes, peakHeights)
          }

        val ratios = properties
          // outlier-removed values in second collection
          .map(x => (x, Statistics.eliminateOutliers(x)))

          // ensure both collections have at least 2 elements to avoid NaNs
          .filter { case (x, filtered) => x.size > 1 && filtered.size > 1 }

          // calculate rsd ratios
          .map { case (x, filtered) => Statistics.rsdDev(filtered) / Statistics.rsdDev(x) }


        if (ratios.isEmpty) {
          logger.warn("Unable to compute loss function due to lack of shared target values")
          (target, Double.NaN)
        } else {
          // calculate combined error and scale by missing targets
          val errorSquared = ratios.map(x => math.pow(x, 2)).sum / (ratios.length / properties.length)
          (target, math.sqrt(errorSquared))
        }
    }

    // ratio of annotation count to maximum number of possible annotations
    val scaling = calculateScalingByTargetCount(samples, data, Some(data.size))

    Statistics.mean(rsd.values.filter(!_.isNaN)) / scaling
  }
}


class STDOutlierCorrectionLossFunction extends STDOutlierLossFunction[CorrectedSample] {

  def lossFunction(corrected: List[CorrectedSample]): Double = {
    val targetsAndAnnotationsForAllSamples = getTargetsAndAnnotationsForCorrectedSamples(corrected)
    stdOutlierRsd(corrected, targetsAndAnnotationsForAllSamples)
  }
}

class STDOutlierAnnotationLossFunction extends STDOutlierLossFunction[AnnotatedSample] {

  def lossFunction(annotated: List[AnnotatedSample]): Double = {
    val targetsAndAnnotationsForAllSamples = getTargetsAndAnnotationsForAnnotatedSamples(annotated)
    stdOutlierRsd(annotated, targetsAndAnnotationsForAllSamples)
  }
}
