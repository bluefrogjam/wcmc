package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms

import javax.validation.constraints._

import scala.beans.BeanProperty

class LCMSRetentionIndexTargetProperties {
  @BeanProperty
  @NotBlank
  var identifier: String = "unknown"

  @BeanProperty
  @DecimalMax("10000000.0")
  @DecimalMin("0.0")
  var retentionIndex: Double = 0.0

  @BeanProperty
  @NotNull
  @DecimalMin("0.0")
  @DecimalMax("5000.0")
  var accurateMass: Double = 0.0

  @BeanProperty
  @NotNull
  @DecimalMin("0.0")
  @DecimalMax("20.0")
  var retentionTime: Float = 0.0f

  @BeanProperty
  @Pattern(regexp = "minutes|seconds")
  var retentionTimeUnit: String = "minutes"

  @BeanProperty
  @NotNull
  var isInternalStandard: Boolean = false

  @BeanProperty
  @NotNull
  var requiredForCorrection: Boolean = false

  @BeanProperty
  @NotNull
  var confirmed: Boolean = false
}
