package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, MergeLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.springframework.beans.factory.annotation.{Qualifier, Value}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.{SpringApplication, WebApplicationType}
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.http.MediaType
import org.springframework.web.servlet.config.annotation.{ContentNegotiationConfigurer, CorsRegistry, WebMvcConfigurer}

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
  def workflow: Workflow[Double] = new Workflow[Double]

  @Bean
  def mergedLibrary(correction: LibraryAccess[CorrectionTarget], @Qualifier("monaLibraryAccess") annotation: LibraryAccess[AnnotationTarget]): MergeLibraryAccess = new MergeLibraryAccess(correction, annotation)
}

object Carrot extends App {
  val app = new SpringApplication(classOf[Carrot])
  app.setWebApplicationType(WebApplicationType.SERVLET)
  val context = app.run(args: _*)
}

@Configuration
class CarrotCors extends WebMvcConfigurer {

  override def configureContentNegotiation(configurer: ContentNegotiationConfigurer): Unit = {
    configurer.favorPathExtension(false)
        .favorParameter(false)
        .parameterName("mediaType")
        .ignoreAcceptHeader(false)
        .useRegisteredExtensionsOnly(true)
        .defaultContentType(MediaType.APPLICATION_JSON)
        .mediaType("json", MediaType.APPLICATION_JSON)
  }

  override def addCorsMappings(registry: CorsRegistry): Unit = registry.addMapping("/**")
}

@Configuration
@EnableCaching(proxyTargetClass = true)
class CarrotRedisCache {
}
