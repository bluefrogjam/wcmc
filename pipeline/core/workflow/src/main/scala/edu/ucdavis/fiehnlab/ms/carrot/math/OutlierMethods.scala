package edu.ucdavis.fiehnlab.ms.carrot.math

import Numeric.Implicits._

object OutlierMethods {

  /**
    * recursively removes outliers from a dataset outside of stdThreshold * stdDev from the mean
    *
    * @param data
    * @param f accessor function
    * @oaram stdThreshold
    * @tparam T
    */
  def eliminateOutliers[T, S: Numeric](data: Iterable[T], f: T => S, stdThreshold: Double = 3): Iterable[T] = {
    val mean = data.map(x => f(x)).sum.toDouble / data.size
    val stdDev = math.sqrt(data.map(x => f(x).toDouble).map(a => math.pow(a - mean, 2)).sum / (data.size - 1))

    if (data.size < 3) {
      data
    } else {
      val filteredData = data.filter(x => math.abs(f(x).toDouble - mean) <= stdThreshold * stdDev)

      if (data.size == filteredData.size) {
        data
      } else {
        eliminateOutliers(filteredData, f, stdThreshold)
      }
    }
  }
}
