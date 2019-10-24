package edu.ucdavis.fiehnlab.ms.carrot.core.workflow

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.io.ConversionAwareSampleLoader
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation._

/**
  * Created by wohlgemuth on 7/14/17.
  */
@Configuration
@ComponentScan
@EnableConfigurationProperties
@EnableCaching
class CentralWorkflowConfig extends Logging {
}
