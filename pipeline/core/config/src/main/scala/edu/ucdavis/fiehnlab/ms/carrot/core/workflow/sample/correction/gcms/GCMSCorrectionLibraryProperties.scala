package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms

import java.util

import javax.validation.Valid
import javax.validation.constraints.{Min, Size}
import org.springframework.boot.context.properties.{ConfigurationProperties, NestedConfigurationProperty}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

import scala.beans.BeanProperty

@Component
@Validated
@Profile(Array("carrot.gcms"))
@ConfigurationProperties(prefix = "wcmc.workflow.gcms.library.correction", ignoreUnknownFields = false, ignoreInvalidFields = false)
class GCMSCorrectionLibraryProperties {

  /**
    * all our targets
    */
  @Valid
  @Size(min = 1)
  @BeanProperty
  @NestedConfigurationProperty
  var config: java.util.List[GCMSLibraryConfiguration] = new util.ArrayList[GCMSLibraryConfiguration]()

  /**
    * minimum found standards
    */
  @Min(1)
  @BeanProperty
  var requiredStandards: Int = 6

}
