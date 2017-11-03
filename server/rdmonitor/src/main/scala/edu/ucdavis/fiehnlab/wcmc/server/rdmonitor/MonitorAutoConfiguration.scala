package edu.ucdavis.fiehnlab.wcmc.server.rdmonitor

import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class MonitorAutoConfiguration {
  @Bean
  val monitor: Monitor = new Monitor()
}
