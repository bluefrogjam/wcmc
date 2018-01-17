package edu.ucdavis.fiehnlab.cts3

import org.springframework.context.annotation.{Bean, Configuration}
import springfox.documentation.builders.{PathSelectors, RequestHandlerSelectors}
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

/**
  * Created by diego on 01/17/2018
  **/
@Configuration
@EnableSwagger2
class SwaggerConfiguration {
  @Bean
  def api(): Docket = {
    new Docket(DocumentationType.SWAGGER_2).select.apis(RequestHandlerSelectors.any).paths(PathSelectors.any).build
  }
}