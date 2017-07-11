package edu.ucdavis.fiehnlab.server.fserv

import java.io.File

import edu.ucdavis.fiehnlab.loader.LocalLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.wcms.utilities.casetojson.config.CaseClassToJSONSerializationConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.context.annotation.{Bean, Import}

/**
  * Created by wohlgemuth on 7/7/17.
  */
@SpringBootApplication
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[CaseClassToJSONSerializationConfiguration]))
class FServ {

  @Value("${wcms.server.fserv.directory:storage}")
  val directory: String = null

  @Bean
  def resourceLoader: LocalLoader = new RecursiveDirectoryResourceLoader(new File(directory))
}


object FServ extends App {

  val app = new SpringApplication(classOf[FServ])
  app.setWebEnvironment(true)
  val context = app.run(args: _*)

}
