package edu.ucdavis.fiehnlab.ms.carrot.core.db.yaml

import org.apache.logging.log4j.scala.Logging
import org.springframework.boot.context.properties.{ConfigurationProperties, EnableConfigurationProperties}
import org.springframework.context.annotation.{ComponentScan, Configuration, Profile}
import org.springframework.stereotype.Component

import scala.beans.BeanProperty


@Configuration
@ComponentScan
@EnableConfigurationProperties
@Profile(Array("carrot.targets.yaml.annotation", "carrot.targets.yaml.correction"))
class YAMLLibraryAutoConfiguration extends Logging

@Component
@ConfigurationProperties(prefix = "carrot.targets.yaml.properties")
@Profile(Array("carrot.targets.yaml.annotation", "carrot.targets.yaml.correction"))
class YAMLLibraryConfigurationProperties extends Logging {
  logger.info("creating yaml configuration properties")
  /**
    * which resource you would like to load
    */
  @BeanProperty
  var resource: String = "libraries.yml"
}
