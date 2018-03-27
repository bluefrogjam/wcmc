package edu.ucdavis.fiehnlab.cts3

import com.typesafe.scalalogging.LazyLogging
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}

/**
  * Created by diego on 1/12/2018
  **/
@SpringBootApplication
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
class Cts extends LazyLogging {
    logger.info("Launching CTS-3")
}

object Cts extends App {
  val app = new SpringApplication(classOf[Cts])
  app.setWebEnvironment(true)
  val context = app.run(args: _*)
}
