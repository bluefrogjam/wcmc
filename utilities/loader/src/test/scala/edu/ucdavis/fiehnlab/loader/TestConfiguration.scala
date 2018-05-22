package edu.ucdavis.fiehnlab.loader

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.context.annotation.ComponentScan

/**
  * Created by wohlg on 7/28/2016.
  */
@SpringBootApplication
@ComponentScan(basePackageClasses = Array(classOf[ResourceLoader]))
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
class TestConfiguration {

}
