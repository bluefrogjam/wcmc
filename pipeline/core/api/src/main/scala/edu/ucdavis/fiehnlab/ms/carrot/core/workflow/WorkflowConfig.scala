package edu.ucdavis.fiehnlab.ms.carrot.core.workflow

import edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config.CaseClassToJSONSerializationConfiguration
import org.springframework.context.annotation.{ComponentScan, Configuration, Import}

/**
  * Created by wohlgemuth on 6/26/16.
  */
@Configuration
@ComponentScan
@Import(Array(classOf[CaseClassToJSONSerializationConfiguration]))
class WorkflowConfig {

}
