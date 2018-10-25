package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms

import javax.validation.constraints.{DecimalMax, DecimalMin}

import scala.beans.BeanProperty

class RatioConfiguration {
  @BeanProperty
  @DecimalMax("10.0")
  @DecimalMin("-10.0")
  var min: Double = 0.0

  @BeanProperty
  @DecimalMax("10.0")
  @DecimalMin("-10.0")
  var max: Double = 0.0
}
