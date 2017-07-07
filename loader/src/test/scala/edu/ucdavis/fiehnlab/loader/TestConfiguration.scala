package edu.ucdavis.fiehnlab.loader

import org.springframework.context.annotation.{ComponentScan, Configuration}

/**
  * Created by wohlg on 7/28/2016.
  */
@Configuration
@ComponentScan(basePackageClasses = Array(classOf[ResourceLoader]))
class TestConfiguration {

}
