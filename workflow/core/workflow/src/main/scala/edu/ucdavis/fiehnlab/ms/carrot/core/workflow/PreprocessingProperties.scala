package edu.ucdavis.fiehnlab.ms.carrot.core.workflow

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing.PuritySettings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
  * all the preprocessing configurations
  */
@Component
@ConfigurationProperties(prefix = "processing")
class PreprocessingProperties {

  @Autowired
  val purity: PuritySettings = null
}
