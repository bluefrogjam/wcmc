package edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j

import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
class MonitorConfig {
  @Bean
  def fserv4j: FServ4jClient = new FServ4jClient("testfserv.fiehnlab.ucdavis.edu")
}
