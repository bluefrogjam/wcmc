package edu.ucdavis.fiehnlab.cts3

import edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config.CaseClassToJSONSerializationAutoConfiguration
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}
import org.springframework.web.client.{RestOperations, RestTemplate}

/**
  * Created by diego on 1/17/2018
  **/
@Configuration
@ComponentScan
@Import(Array(classOf[CaseClassToJSONSerializationAutoConfiguration]))
class Cts3AutoConfiguration {
}
