package edu.ucdavis.fiehnlab.ms.carrot.core.db.yaml

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.ClasspathResourceLoader
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}

@Configuration
@ComponentScan
@EnableConfigurationProperties
@Profile(Array("test", "carrot.targets.yaml.annotation", "carrot.targets.yaml.correction"))
class YAMLLibraryAccessTestConfiguration {
  @Bean
  @Profile(Array("!carrot.resource.loader.bucket"))
  def loalLoader: ResourceLoader = new ClasspathResourceLoader()
}
