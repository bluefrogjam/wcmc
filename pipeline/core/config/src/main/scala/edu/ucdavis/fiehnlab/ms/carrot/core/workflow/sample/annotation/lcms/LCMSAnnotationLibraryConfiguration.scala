package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.lcms

import java.util

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSRetentionIndexTargetProperties
import javax.validation.Valid
import javax.validation.constraints.{NotBlank, Pattern, Size}
import org.springframework.boot.context.properties.NestedConfigurationProperty

import scala.beans.BeanProperty

class LCMSAnnotationLibraryConfiguration {
  @BeanProperty
  @Valid
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

  @BeanProperty
  var minimumPeakIntensity: Float = 0f

}
