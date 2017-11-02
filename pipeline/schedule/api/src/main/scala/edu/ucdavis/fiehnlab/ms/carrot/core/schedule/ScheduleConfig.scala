package edu.ucdavis.fiehnlab.ms.carrot.core.schedule

import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}

/**
  * Created by wohlgemuth on 9/15/17.
  */
@Configuration
class ScheduleConfig {

  @Bean
  def taskRunner:TaskRunner = new TaskRunner
}
