package edu.ucdavis.fiehnlab.ms.carrot.core.api.math

/**
  * defines a basic regression curve
  * and implementation define the exact behavior
  */
trait Regression {
  def coefficient: Array[Double]

  def calibration(x: Array[Double], y: Array[Double])

  def getXCalibrationData: Array[Double]

  def getYCalibrationData: Array[Double]

  def computeY(x: Double): Double

  def getFormulas: Array[String]

}