package edu.ucdavis.fiehnlab.ms.carrot.core.msdial

import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.io.ResourceLoaderSampleLoader
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
@EnableCaching
class LCTestConfig {

  @Bean
  def loader(delegatingResourceLoader: DelegatingResourceLoader): ResourceLoaderSampleLoader = new ResourceLoaderSampleLoader(delegatingResourceLoader)
}
