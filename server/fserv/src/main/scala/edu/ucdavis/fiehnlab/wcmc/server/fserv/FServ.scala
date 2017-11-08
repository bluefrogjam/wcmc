package edu.ucdavis.fiehnlab.wcmc.server.fserv

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.LocalLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.wcmc.server.fserv.controller.FServController
import edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config.CaseClassToJSONSerializationAutoConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.web.servlet.config.annotation.{ContentNegotiationConfigurer, CorsRegistry, WebMvcConfigurerAdapter}

/**
	* Created by wohlgemuth on 7/7/17.
	*/
@SpringBootApplication
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
//@Import(Array(classOf[CaseClassToJSONSerializationAutoConfiguration]))
class FServ extends WebMvcConfigurerAdapter{

  override def configureContentNegotiation(configurer: ContentNegotiationConfigurer): Unit = {
    configurer.favorPathExtension(false).favorParameter(false).parameterName("mediaType").ignoreAcceptHeader(false).useJaf(false).defaultContentType(MediaType.APPLICATION_JSON).mediaType("json", MediaType.APPLICATION_JSON)
  }

  override def addCorsMappings(registry: CorsRegistry): Unit = registry.addMapping("/**")
}

@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses= Array(classOf[FServController]))
class FServConfig extends WebSecurityConfigurerAdapter with LazyLogging {

  @Value("${wcmc.server.fserv.directory:storage}")
  val directory: String = null

  @Bean
  def resourceLoader: LocalLoader = new RecursiveDirectoryResourceLoader(new File(directory))

	override def configure(web: WebSecurity): Unit = {
		logger.warn("we are allowing unregulated access to this service!")
		web.ignoring().antMatchers("/**")
	}
}
