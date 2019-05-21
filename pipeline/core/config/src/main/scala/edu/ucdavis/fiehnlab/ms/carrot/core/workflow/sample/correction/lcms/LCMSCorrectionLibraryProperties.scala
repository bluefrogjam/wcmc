package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms

import java.util

import javax.validation.Valid
import javax.validation.constraints.{Min, Size}
import org.springframework.boot.context.properties.{ConfigurationProperties, NestedConfigurationProperty}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

import scala.beans.BeanProperty

@Component
@Profile(Array("carrot.lcms"))
@ConfigurationProperties(prefix = "wcmc.workflow.lcms.library.correction", ignoreUnknownFields = false, ignoreInvalidFields = false)
class LCMSCorrectionLibraryProperties {

  /**
    * all our targets
    */
  @Valid
  @Size(min = 1)
  @BeanProperty
  @NestedConfigurationProperty
  var config: java.util.List[LCMSLibraryConfiguration] = new util.ArrayList[LCMSLibraryConfiguration]()

  /**
    * minimum found standards
    */
  @Min(1)
  @BeanProperty
  var requiredStandards: Int = 6

}
