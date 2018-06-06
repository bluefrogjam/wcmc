package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms

import javax.validation.constraints._

import scala.beans.BeanProperty

class LCMSRetentionIndexTargetConfiguration {
  @BeanProperty
  @NotBlank
  var identifier: String = _

  @BeanProperty
  @DecimalMax("10000000.0")
  @DecimalMin("0.0")
  var retentionIndex: Double = _

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
  @NotNull
  var retensionTimeUnit: String = _

  @BeanProperty
  @NotNull
  var isInternalStandard: Boolean = _

  @BeanProperty
  @NotNull
  var requiredForCorrection: Boolean = _

  @BeanProperty
  @NotNull
  var confirmed: Boolean = _
}
