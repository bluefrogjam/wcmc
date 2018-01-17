package edu.ucdavis.fiehnlab.cts3

import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.web.client.{RestOperations, RestTemplate}

/**
  * Created by diego on 1/17/2018
  **/
@Configuration
@ComponentScan
class Cts3AutoConfiguration {
  @Bean
  def template: RestOperations = new RestTemplate()
}
