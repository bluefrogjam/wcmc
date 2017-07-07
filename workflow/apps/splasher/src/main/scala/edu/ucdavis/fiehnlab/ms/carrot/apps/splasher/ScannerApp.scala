package edu.ucdavis.fiehnlab.ms.carrot.apps.splasher

import java.io.{File, FileOutputStream}
import java.util.zip.GZIPOutputStream

import akka.actor.ActorSystem
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.scanner.SampleScanner
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.scanner.listeners.{SampleScannerListener, SplashFileGenerator}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.{CommandLineRunner, SpringApplication}
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

/**
  * Created by wohlg_000 on 4/27/2016.
  */
@SpringBootApplication
class ScannerApp {

  @Bean
  def actorSystem: ActorSystem = {
    ActorSystem.create("directory-scanner")
  }

  @Bean
  def sampleScanner(actorSystem: ActorSystem, listeners:List[SampleScannerListener]): SampleScanner = {
    new SampleScanner(actorSystem,listeners)
  }

  @Bean
  def listener: List[SampleScannerListener] = {
    new SplashFileGenerator(new FileOutputStream("splashed.txt")) :: List()
  }

}

/**
  * runs the scanner in the current directory and sub directories
  */
@Component
class ScannerCommandLineRunner extends CommandLineRunner {

  @Autowired
  val sampleScanner: SampleScanner = null

  @Autowired
  val actorSystem: ActorSystem = null

  override def run(strings: String*): Unit = {

    if (strings.size == 0) {

      sampleScanner.scan(new File("./"))
    }
    else {
      sampleScanner.scan(new File(strings(0)))
    }
  }
}


/**
  * Created by wohlg_000 on 4/27/2016.
  */
object ScannerApp extends App {

  val app = new SpringApplication(classOf[ScannerApp])
  app.setWebEnvironment(false)
  val context = app.run(args: _*)

}
