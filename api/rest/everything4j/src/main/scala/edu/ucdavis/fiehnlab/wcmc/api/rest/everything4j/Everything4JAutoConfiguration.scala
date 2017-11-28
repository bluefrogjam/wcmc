package edu.ucdavis.fiehnlab.wcmc.api.rest.everything4j

import org.springframework.context.annotation.{Bean, Configuration}

/**
  * Created by wohlgemuth on 10/11/17.
  */
@Configuration
class Everything4JAutoConfiguration {

  @Bean
  def everything4J: Everything4J = new Everything4J()
}
