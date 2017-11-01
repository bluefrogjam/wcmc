package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.rdmonitor

import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class MonitorAutoConfiguration {
  @Bean
  val monitor: Monitor = new Monitor()

  @Bean
  val dfClient: DataFormerClient = new DataFormerClient()
}
