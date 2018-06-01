package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.lcms.correction

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
  var name: String = _

  @BeanProperty
  var description: String = _

  @BeanProperty
  @NotBlank
  var column: String = _

  @BeanProperty
  @NotBlank
  var instrument: String = _

  /**
    * how high do peaks have to be to be considered as targets
    * for RI correction
    */
  @BeanProperty
  var minimumPeakIntensity: Float = 0

}
