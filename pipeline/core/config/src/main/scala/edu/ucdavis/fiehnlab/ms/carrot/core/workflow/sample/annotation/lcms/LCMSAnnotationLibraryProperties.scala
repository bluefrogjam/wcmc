package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.lcms

import java.util

import javax.validation.Valid
import org.springframework.boot.context.properties.{ConfigurationProperties, NestedConfigurationProperty}
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

import scala.beans.BeanProperty

@Component
@Validated
@Profile(Array("carrot.lcms"))
@ConfigurationProperties(prefix = "carrot.lcms.annotation", ignoreUnknownFields = false, ignoreInvalidFields = false)
case class LCMSAnnotationLibraryProperties() {

  /**
    * all our targets
    */
  @Valid
  @BeanProperty
  @NestedConfigurationProperty
  var config: java.util.List[LCMSAnnotationLibraryConfiguration] = new util.ArrayList[LCMSAnnotationLibraryConfiguration]()
}
