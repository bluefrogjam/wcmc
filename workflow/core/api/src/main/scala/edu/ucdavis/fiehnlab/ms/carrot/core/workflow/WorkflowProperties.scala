package edu.ucdavis.fiehnlab.ms.carrot.core.workflow

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
  * main configuration object
  */
@Component
@ConfigurationProperties(prefix = "workflow")
class WorkflowProperties {

  /**
    * should we track all the changes applied to samples during processing,
    * warning requires a lot of memory and should only be used for debugging
    * purposes
    */
    @Deprecated
  val trackChanges: Boolean = false
}





