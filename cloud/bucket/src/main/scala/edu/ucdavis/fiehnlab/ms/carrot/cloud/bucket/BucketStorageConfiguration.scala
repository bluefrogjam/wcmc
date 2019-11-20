package edu.ucdavis.fiehnlab.ms.carrot.cloud.bucket

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import edu.ucdavis.fiehnlab.loader.{ResourceLoader, ResourceStorage}
import edu.ucdavis.fiehnlab.ms.carrot.cloud.aws.ASWConfigurationProperties
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.context.properties.{ConfigurationProperties, EnableConfigurationProperties}
import org.springframework.context.annotation.{Bean, Configuration, Profile}
import org.springframework.stereotype.Component

import scala.beans.BeanProperty

@Profile(Array("carrot.resource.store.bucket.data",
  "carrot.resource.store.bucket.result"))
@ConfigurationProperties(prefix = "wcmc.workflow.resource.store.bucket")
@Component
class BucketStorageConfigurationProperties {

  @Value("${wcmc.workflow.resource.store.bucket.name:data-carrot}")
  var name: String = _

  @BeanProperty
  val timeOfLife: Long = 0

  @Value("${wcmc.workflow.resource.store.bucket.result:wcmc-data-stasis-result-prod}")
  var result: String = _

  override def toString: String = s"name: $name, results: $result, TTL: $timeOfLife"
}

@EnableConfigurationProperties
@Configuration
class BucketStorageConfiguration {
  @Autowired
  val awsProperties: ASWConfigurationProperties = null

  @Autowired
  val amazonAWSCredentials: AWSCredentialsProvider = null

  @Autowired
  val properties: BucketStorageConfigurationProperties = null

  @Bean
  def client: AmazonS3 = AmazonS3ClientBuilder.standard.withRegion(awsProperties.region).withCredentials(amazonAWSCredentials).build

  @Profile(Array("carrot.resource.store.bucket.data"))
  @Bean(name = Array("dataStorage"))
  def dataStorage: ResourceStorage = new InputBucketStorage(client, properties, awsProperties)

  @Profile(Array("carrot.resource.store.bucket.result"))
  @Bean(name = Array("outputStorage"))
  def outputStorage: ResourceStorage = new OutputBucketStorage(client, properties, awsProperties)

  @Profile(Array("carrot.resource.loader.bucket.data"))
  @Bean(name = Array("dataLoader"))
  def dataLoader: ResourceLoader = new InputBucketLoader(client, properties, awsProperties)

  @Profile(Array("carrot.resource.loader.bucket.result"))
  @Bean(name = Array("resultLoader"))
  def resultLoader: ResourceLoader = new OutputBucketLoader(client, properties, awsProperties)
}
