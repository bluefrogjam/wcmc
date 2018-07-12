package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Bean

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
//@Import(Array(classOf[RestClientConfig]))
class RunnerTestConfig {
  @Bean
  def runner: Runner = new Runner()
}
