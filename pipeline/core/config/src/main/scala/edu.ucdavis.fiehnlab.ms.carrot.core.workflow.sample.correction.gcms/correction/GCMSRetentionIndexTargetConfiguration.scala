package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms.correction

import java.util

import javax.validation.Valid
import javax.validation.constraints._
import org.springframework.boot.context.properties.NestedConfigurationProperty

import scala.beans.BeanProperty

class GCMSRetentionIndexTargetConfiguration {
  @BeanProperty
  @NotBlank
  var identifier: String = _

  @BeanProperty
  @DecimalMax("10000000.0")
  @DecimalMin("0.0")
  var retentionIndex: Double = 0.0

  @BeanProperty
  @DecimalMax("1000000.0")
  @DecimalMin("0.0")
  var minApexSn: Double = 0.0

  @BeanProperty
  @DecimalMax("5000.0")
  @DecimalMin("0.0")
  var qualifierIon: Double = 0.0

  @BeanProperty
  @DecimalMax("10.0")
  @DecimalMin("-10.0")
  var minQualifierRatio: Double = 0.0

  @BeanProperty
  @DecimalMax("10.0")
  @DecimalMin("-10.0")
  var maxQualifierRatio: Double = 0.0

  @BeanProperty
  @Valid
  @Size(min = 1)
  @NestedConfigurationProperty
  val distanceRatios: java.util.List[RatioConfiguration] = new util.ArrayList[RatioConfiguration]()

  @BeanProperty
  @DecimalMax("1.0")
  @DecimalMin("0.0")
  var minSimilarity: Double = 0.0

  @BeanProperty
  @NotNull
  var required: Boolean = false

  @BeanProperty
  @DecimalMax("5000.0")
  @DecimalMin("0.0")
  var uniqueMass: Double = 0.0

  @BeanProperty
  @NotBlank
  var spectra: String = _

  @BeanProperty
  var validationTarget: Boolean = true

}
