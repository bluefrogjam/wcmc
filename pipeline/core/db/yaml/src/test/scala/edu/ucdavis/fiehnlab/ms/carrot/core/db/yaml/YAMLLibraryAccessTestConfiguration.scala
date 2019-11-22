package edu.ucdavis.fiehnlab.ms.carrot.core.db.yaml

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.ClasspathResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.cloud.bucket.BucketStorageConfigurationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.{Bean, Profile}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
@EnableConfigurationProperties
class YAMLLibraryAccessTestConfiguration {
  @Bean
  @Profile(Array("!carrot.resource.loader.bucket.data"))
  def localLoader: ResourceLoader = new ClasspathResourceLoader()

  @Bean
  def bucketProperties: BucketStorageConfigurationProperties = new BucketStorageConfigurationProperties
}
