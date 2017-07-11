package edu.ucdavis.fiehnlab.ms.carrot.core.workflow

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
  * all LCMS target based properties and settings
  */
@Component
@ConfigurationProperties(prefix = "lcms")
class LCMSProperties {

  /**
    * this allows to automatically correct the sample, with fallback methods in case it fails
    */
  var allowCorrectionFailedFallback: Boolean = true

  /**
    * annotation properties for lcms target annotation
    */
  @Autowired
  val annotation: LCMSTargetAnnotationProperties = null
}
