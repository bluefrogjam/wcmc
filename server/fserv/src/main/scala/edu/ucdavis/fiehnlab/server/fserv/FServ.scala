package edu.ucdavis.fiehnlab.server.fserv

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}

/**
  * Created by wohlgemuth on 7/7/17.
  */
@SpringBootApplication
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
class FServ {

}


object FServ extends App{

  val app = new SpringApplication(classOf[FServ])
  app.setWebEnvironment(false)
  val context = app.run(args: _*)

}
