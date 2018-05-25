package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms

import java.util

import javax.validation.Valid
import javax.validation.constraints.{NotBlank, Size}
import org.springframework.boot.context.properties.NestedConfigurationProperty

import scala.beans.BeanProperty

class LCMSLibraryConfiguration {

  @BeanProperty
  @Valid
  @Size(min = 1)
  @NestedConfigurationProperty
  val targets: java.util.List[LCMSRetentionIndexTargetConfiguration] = new util.ArrayList[LCMSRetentionIndexTargetConfiguration]()

  @BeanProperty
  @NotBlank
  var name: String = ""

  @BeanProperty
  @NotBlank
  var description: String = ""

  @BeanProperty
  @NotBlank
  var column: String = ""

  @BeanProperty
  @NotBlank
  var instrument: String = ""

  /**
    * how high do peaks have to be to be considered as targets
    * for RI correction
    */
  @BeanProperty
  var minimumPeakIntensity: Float = 0

}
