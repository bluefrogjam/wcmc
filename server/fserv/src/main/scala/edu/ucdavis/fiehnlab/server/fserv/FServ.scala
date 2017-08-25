package edu.ucdavis.fiehnlab.server.fserv

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.LocalLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.wcms.utilities.casetojson.config.CaseClassToJSONSerializationConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.{HttpSecurity, WebSecurity}
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.web.servlet.config.annotation.{ContentNegotiationConfigurer, WebMvcConfigurerAdapter}

/**
  * Created by wohlgemuth on 7/7/17.
  */
@SpringBootApplication
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[CaseClassToJSONSerializationConfiguration]))
class FServ extends WebMvcConfigurerAdapter{

  @Value("${wcms.server.fserv.directory:storage}")
  val directory: String = null

  @Bean
  def resourceLoader: LocalLoader = new RecursiveDirectoryResourceLoader(new File(directory))

  override def configureContentNegotiation(configurer: ContentNegotiationConfigurer): Unit = {
    configurer.favorPathExtension(false).favorParameter(false).parameterName("mediaType").ignoreAcceptHeader(false).useJaf(false).defaultContentType(MediaType.APPLICATION_JSON).mediaType("json", MediaType.APPLICATION_JSON)
  }
}

@Configuration
@EnableWebSecurity
class FServSecurity extends WebSecurityConfigurerAdapter with LazyLogging{


  override def configure(web: WebSecurity): Unit = {
    logger.warn("we are allowing unregulated access to this service!")
    web.ignoring().antMatchers("/**")
  }
}


object FServ extends App {

  val app = new SpringApplication(classOf[FServ])
  app.setWebEnvironment(true)
  val context = app.run(args: _*)

}
