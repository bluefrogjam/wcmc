package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, MSSpectra, MetadataSupport}

trait LossFunctions {

  /**
    * Return a list of all annotated correction features grouped by metabolite
    * @param corrected
    * @return
    */
  private def getTargetsAndAnnotationsForAllSamples(corrected: List[CorrectedSample]): Map[Target, List[(Target, Feature)]] = {
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
    * calculates the average rsd in peak height by metabolite over all samples
    * @param corrected
    * @return
    */
  def peakHeightRsdLossFunction(corrected: List[CorrectedSample]): Double = {
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


  /**
    * calculates a error value based on the presence of outliers in mass, retention time or peak height
    * @param corrected
    * @param usePeakHeight
    * @return
    */
  def mzAndRTOutlierLossFunction(corrected: List[CorrectedSample], usePeakHeight: Boolean = true): Double = {
    val targetsAndAnnotationsForAllSamples = getTargetsAndAnnotationsForAllSamples(corrected)

    // for each metabolite, calculate the ratio of the rsd of all annotations by the rsd of
    // the outlier filtered annotations.  high values indicate larger spread of the data before
    // outlier removal and therefore the presence of mis-annotations.  a ratio of 1 indicates
    // no outliers and therefore a reasonable confidence of good annotations
    val rsd = targetsAndAnnotationsForAllSamples.map {
      item =>
        val annotations = item._2.map(_._2)

        val peakHeights: List[Double] = annotations.collect {
          case feature: MSSpectra with MetadataSupport =>
            feature.metadata("peakHeight").asInstanceOf[Some[Double]].get
        }

        val retentionTimes: List[Double] = annotations.collect {
          case feature: MSSpectra with MetadataSupport =>
            feature.metadata("peakRTmin").asInstanceOf[Some[Double]].get
        }

        val accurateMasses: List[Double] = annotations.collect {
          case feature: MSSpectra with MetadataSupport =>
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

        // combine ratios using euclidean metric
        val error = math.sqrt(
          if (usePeakHeight) {
            math.pow(peakHeightRsdRatio, 2) + math.pow(retentionTimeRsdRatio, 2) + math.pow(accurateMassRsdRatio, 2)
          } else {
            math.pow(retentionTimeRsdRatio, 2) + math.pow(accurateMassRsdRatio, 2)
          }
        )

        (item._1, error)
    }

    val averageError = Statistics.mean(rsd.values)

    averageError
  }
}