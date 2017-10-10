package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.db.mona.MonaLibraryAccessConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.ScheduleConfig
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted.LCMSPositiveModeTargetWorkflow
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.CentralWorkflowConfig
import edu.ucdavis.fiehnlab.wcmc.schedule.server.controller.SchedulingController
import edu.ucdavis.fiehnlab.wcmc.server.fserv.controller.FServController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.web.servlet.config.annotation.{ContentNegotiationConfigurer, CorsRegistry, WebMvcConfigurerAdapter}

/**
  * Created by wohlgemuth on 9/7/17.
  */
@SpringBootApplication(scanBasePackageClasses = Array(classOf[SchedulingController], classOf[ScheduleConfig], classOf[FServController], classOf[CentralWorkflowConfig], classOf[MonaLibraryAccessConfiguration],classOf[Carrot]))
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
class Carrot {

  @Autowired
  val resourceLoader: DelegatingResourceLoader = null

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
