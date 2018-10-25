package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms

import java.util

import javax.validation.Valid
import javax.validation.constraints.{NotBlank, Pattern, Size}
import org.springframework.boot.context.properties.NestedConfigurationProperty

import scala.beans.BeanProperty

class LCMSLibraryConfiguration {

  @BeanProperty
  @Valid
  @Size(min = 1)
  @NestedConfigurationProperty
  val targets: java.util.List[LCMSRetentionIndexTargetProperties] = new util.ArrayList[LCMSRetentionIndexTargetProperties]()

  @BeanProperty
  @NotBlank
  var name: String = ""

  @BeanProperty
  var description: String = ""

  @BeanProperty
  @NotBlank
  var column: String = ""

  @BeanProperty
  @NotBlank
  var instrument: String = ""

  @BeanProperty
  @Pattern(regexp="positive|negative")
  var ionMode: String = "positive"

  /**
    * how high do peaks have to be to be considered as targets
    * for RI correction
    */
  @BeanProperty
  var minimumPeakIntensity: Float = 0f

  println(s" === creating correction library ${name} (${targets.size()} targets)=== ")
}
