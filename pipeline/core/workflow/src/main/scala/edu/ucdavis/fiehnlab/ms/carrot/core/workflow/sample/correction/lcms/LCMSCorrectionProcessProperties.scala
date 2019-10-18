package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

import scala.beans.BeanProperty

/**
  * targeted retention index correction, should be refactored to be a super class to make things easier
  */
@Component
@Profile(Array("carrot.lcms"))
@ConfigurationProperties(prefix = "wcmc.workflow.lcms.process.correction", ignoreUnknownFields = false, ignoreInvalidFields = false)
class LCMSCorrectionProcessProperties {

  @BeanProperty
  var massAccuracySetting: Double = 0.01

  /**
    * MassAccuracy in PPM for correction target search
    */
  @BeanProperty
  var massAccuracyPPMSetting: Double = 10

  /**
    * Retention time accuracy (in seconds) used in target filtering and similarity calculation
    */
  @BeanProperty
  var rtAccuracySetting: Double = 12

  /**
    * Intensity used for penalty calculation - the peak similarity score for targets below this
    * intensity will be scaled down by the ratio of the intensity to this threshold
    */
  @BeanProperty
  var intensityPenaltyThreshold: Float = 10000

  /**
    * absolute value of the height of a peak, to be considered a retention index marker. This is a hard cut off
    * and will depend on inject volume for these reasons
    */
  @BeanProperty
  var minPeakIntensity: Float = 1000

  /**
    * minimum amount of standards, which have to be defined for this method to work
    */
  @BeanProperty
  var minimumDefinedStandard: Int = 5

  /**
    * this defines how many standards we need to find on minimum
    * for a retention index correction method to be successful
    */
  @BeanProperty
  var minimumFoundStandards: Int = 5
  /**
    * how many data points are required for the linear regression at the beginning and the end of the curve
    */
  @BeanProperty
  var linearSamples: Int = 2

  /**
    * what order is the polynomial regression
    */
  @BeanProperty
  var polynomialOrder: Int = 3

  /**
    * we are utilizing the setting to group close by retention targets. This is mostly required, since we can't guarantee the order
    * if markers, if they come at the same time, but have different ionization and so we rather drop them
    * and only use one ionization product.
    *
    * This step happens after the required attribute was checked and so should not cause any issues with the required standards
    *
    * This setting needs to be provided in seconds
    */
  @BeanProperty
  var groupCloseByRetentionIndexStandardDifference: Int = 25
}