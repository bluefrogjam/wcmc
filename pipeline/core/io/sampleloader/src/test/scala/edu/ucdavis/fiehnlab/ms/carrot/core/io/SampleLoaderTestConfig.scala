package edu.ucdavis.fiehnlab.ms.carrot.core.io

import java.io.File

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
@EnableCaching
class SampleLoaderTestConfig {

  @Bean
  def resourceLoader: ResourceLoader = new RecursiveDirectoryResourceLoader(new File("src"))

}
