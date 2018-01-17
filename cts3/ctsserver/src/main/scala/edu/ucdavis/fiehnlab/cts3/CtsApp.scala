package edu.ucdavis.fiehnlab.cts3

import org.springframework.boot.SpringApplication

/**
  * Created by diego on 1/12/2018
  **/
object CtsApp extends App {
  val app = new SpringApplication(classOf[Cts])
  app.setWebEnvironment(true)
  val context = app.run(args: _*)
}
