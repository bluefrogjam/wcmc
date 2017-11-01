package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.rdmonitor

object MonitorApp {
  def main(args: Array[String]): Unit = {
    val app = new SpringApplication(classOf[Monitor])
    app.setWebEnvironment(false)
    app.run()
  }
}
