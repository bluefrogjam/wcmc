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
  def peakHeightMeanRsd(samples: List[T], data: Map[Target, List[ Feature]], usePeakHeight: Boolean = true): Double = {

    // for each metabolite, calculate the ratio of the rsd of all annotations by the rsd of
    // the outlier filtered annotations.  high values indicate larger spread of the data before
    // outlier removal and therefore the presence of mis-annotations.  a ratio of 1 indicates
    // no outliers and therefore a reasonable confidence of good annotations
    val rsd = data.map {
      item =>
        val annotations = item._2

        val peakHeights: List[Double] = annotations.collect {
          case feature: MSSpectra if feature.metadata.contains("peakHeight") =>
            feature.metadata("peakHeight").asInstanceOf[Some[Double]].get
        }

        val retentionTimes: List[Double] = annotations.collect {
          case feature: MSSpectra with CorrectedSpectra =>
            feature.retentionIndex
        }

        val accurateMasses: List[Double] = annotations.collect {
          case feature: MSSpectra if feature.metadata.contains("ms1AccurateMass") =>
            feature.metadata("ms1AccurateMass").asInstanceOf[Some[Double]].get
        }

        // remove outliers
        val filteredPeakHeights = Statistics.eliminateOutliers(peakHeights)
        val filteredRetentionTime = Statistics.eliminateOutliers(retentionTimes)
        val filteredAccurateMasses = Statistics.eliminateOutliers(accurateMasses)

        // rsd ratios
        val peakHeightRsdRatio = Statistics.rsdDev(filteredPeakHeights) / Statistics.rsdDev(peakHeights)
        val retentionTimeRsdRatio = Statistics.rsdDev(filteredRetentionTime) / Statistics.rsdDev(retentionTimes)
        val accurateMassRsdRatio = Statistics.rsdDev(filteredAccurateMasses) / Statistics.rsdDev(accurateMasses)

        // combine ratios using euclidean norm
        val error = math.sqrt(
          if (usePeakHeight) {
            math.pow(peakHeightRsdRatio, 2) + math.pow(retentionTimeRsdRatio, 2) + math.pow(accurateMassRsdRatio, 2)
          } else {
            math.pow(retentionTimeRsdRatio, 2) + math.pow(accurateMassRsdRatio, 2)
          }
        )

        (item._1, error)
    }

    // ratio of annotation count to maximum number of possible annotations
    val scaling = calculateScalingByTargetCount(samples, data, Some(data.size))

    Statistics.mean(rsd.values) / scaling
  }
}


class STDOutlierCorrectionLossFunction extends STDOutlierLossFunction[CorrectedSample] {

  def lossFunction(corrected: List[CorrectedSample]): Double = {
    val targetsAndAnnotationsForAllSamples = getTargetsAndAnnotationsForCorrectedSamples(corrected)
    peakHeightMeanRsd(corrected, targetsAndAnnotationsForAllSamples)
  }
}

class STDOutlierAnnotationLossFunction extends STDOutlierLossFunction[AnnotatedSample] {

  def lossFunction(annotated: List[AnnotatedSample]): Double = {
    val targetsAndAnnotationsForAllSamples = getTargetsAndAnnotationsForAnnotatedSamples(annotated)
    peakHeightMeanRsd(annotated, targetsAndAnnotationsForAllSamples)
  }
}
