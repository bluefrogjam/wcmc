package edu.ucdavis.fiehnlab.loader

import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.autoconfigure.jdbc.{DataSourceAutoConfiguration, DataSourceTransactionManagerAutoConfiguration}
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.context.annotation.{ComponentScan, Configuration}

/**
  * Created by wohlg on 7/28/2016.
  */
@SpringBootApplication
@ComponentScan(basePackageClasses = Array(classOf[ResourceLoader]))
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
class TestConfiguration {

}
