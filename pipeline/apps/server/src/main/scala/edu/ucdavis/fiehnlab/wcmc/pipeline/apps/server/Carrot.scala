package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectionTarget, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.{SpringApplication, WebApplicationType}
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.http.MediaType
import org.springframework.web.servlet.config.annotation.{ContentNegotiationConfigurer, CorsRegistry, WebMvcConfigurerAdapter}

import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 9/7/17.
  */
@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class Carrot extends LazyLogging {

  @Value("${server.port}")
  val port: Integer = 0

  @Bean
  def client: FServ4jClient = new FServ4jClient(
    "localhost", port
  )

  @Bean
  def library(correctionLibraries: java.util.List[LibraryAccess[Target]]): LibraryAccess[Target] = {

    val result = new DelegateLibraryAccess[Target](correctionLibraries.asScala.map(_.asInstanceOf[LibraryAccess[Target]]).seq.toList.asJava)

    logger.warn(s"${result.libraries}")
    result

  }

  @Bean
  def workflow: Workflow[Double] = new Workflow[Double]
}

object Carrot extends App {
  val app = new SpringApplication(classOf[Carrot])
  app.setWebApplicationType(WebApplicationType.SERVLET)
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
@EnableCaching(proxyTargetClass = true)
class CarrotRedisCache {
}
