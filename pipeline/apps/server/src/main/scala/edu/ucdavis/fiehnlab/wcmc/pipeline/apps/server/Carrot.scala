package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.db.mona.MonaLibraryAccessAutoConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.ScheduleConfig
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.CentralWorkflowConfig
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted.LCMSPositiveModeTargetWorkflow
import edu.ucdavis.fiehnlab.wcmc.server.fserv.controller.FServController
import edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config.CaseClassToJSONSerializationAutoConfiguration
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.web.servlet.config.annotation.{ContentNegotiationConfigurer, CorsRegistry, WebMvcConfigurerAdapter}

/**
  * Created by wohlgemuth on 9/7/17.
  */
@SpringBootApplication( exclude =Array(classOf[DataSourceAutoConfiguration]))
class Carrot {

  /**
    * should be done over a profile TODO
    *
    * @return
    */
  @Bean
  def workflow: LCMSPositiveModeTargetWorkflow[Double] = {
    new LCMSPositiveModeTargetWorkflow()
  }
}

object Carrot extends App {
  val app = new SpringApplication(classOf[Carrot])
  app.setWebEnvironment(true)
  val context = app.run(args: _*)

}

@Configuration
@ComponentScan
class CarrotCors extends WebMvcConfigurerAdapter {

  override def configureContentNegotiation(configurer: ContentNegotiationConfigurer): Unit = {
    configurer.favorPathExtension(false).favorParameter(false).parameterName("mediaType").ignoreAcceptHeader(false).useJaf(false).defaultContentType(MediaType.APPLICATION_JSON).mediaType("json", MediaType.APPLICATION_JSON)
  }

  override def addCorsMappings(registry: CorsRegistry): Unit = registry.addMapping("/**")
}

@Configuration
@EnableWebSecurity
class CarrotSecurity extends WebSecurityConfigurerAdapter with LazyLogging {

  override def configure(web: WebSecurity): Unit = {
    logger.warn("we are allowing unregulated access to this service!")
    web.ignoring().antMatchers("/**")
  }
}
