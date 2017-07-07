package edu.ucdavis.fiehnlab.ms.carrot.core.workflow

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
  * all target based configurations
  */
@Component
@ConfigurationProperties(prefix = "target")
class TargetProperties {


  @Autowired
  val lcms: LCMSProperties = null
}
