package edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j

import org.springframework.boot.SpringApplication

object MonitorApp {
  def main(args: Array[String]): Unit = {
    val app = new SpringApplication(classOf[Monitor])
    app.setWebEnvironment(false)
    app.run()
  }
}
