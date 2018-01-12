package edu.ucdavis.fiehnlab.cts3

import org.springframework.boot.SpringApplication

object CtsApp extends App {

    val app = new SpringApplication(classOf[Cts])
    app.setWebEnvironment(true)
    val context = app.run(args: _*)

}
