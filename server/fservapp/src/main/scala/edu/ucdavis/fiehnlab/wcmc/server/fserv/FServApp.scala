package edu.ucdavis.fiehnlab.wcmc.server.fserv

import org.springframework.boot.SpringApplication

object FServApp extends App {

  val app = new SpringApplication(classOf[FServ])
  app.setWebEnvironment(true)
  val context = app.run(args: _*)

}
