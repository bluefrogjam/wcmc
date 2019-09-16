package edu.ucdavis.fiehnlab.wcmc.api.rest.everything4j

import org.springframework.context.annotation.{Bean, Configuration, Profile}

/**
  * Created by wohlgemuth on 10/11/17.
  */
@Configuration
class Everything4JAutoConfiguration {

  @Profile(Array("file.source.localhost"))
  @Bean
  def everything4J: Everything4J = new Everything4J("localhost", 8585)

  @Profile(Array("file.source.eclipse"))
  @Bean
  def everything4Jlocal: Everything4J = new Everything4J("eclipse.fiehnlab.ucdavis.edu", 80)
}
