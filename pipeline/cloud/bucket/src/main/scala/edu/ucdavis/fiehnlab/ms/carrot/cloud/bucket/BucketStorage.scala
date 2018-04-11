package edu.ucdavis.fiehnlab.ms.carrot.cloud.bucket

import com.amazonaws.auth.{AWSCredentialsProvider, AWSStaticCredentialsProvider}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import java.io.{File, FileInputStream, InputStream}
import java.util.Date
import javax.annotation.PostConstruct

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.s3.model.CreateBucketRequest
import com.amazonaws.services.s3.{AmazonS3, AmazonS3Client}
import edu.ucdavis.fiehnlab.loader.{ResourceLoader, ResourceStorage}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.context.properties.{ConfigurationProperties, EnableConfigurationProperties}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}
import org.springframework.stereotype.Component

import scala.beans.BeanProperty

/**
  * amazon S3 bucket storage access
  */
@Component
@Profile(Array("carrot.resource.store.bucket"))
class BucketStorage @Autowired()(client: AmazonS3, properties: BucketStorageConfigurationProperties) extends ResourceLoader with ResourceStorage {

  @PostConstruct
  def init() = {
    if (!client.doesBucketExist(properties.name)) {
      logger.info(s"creating new bucket with name: ${properties.name}")
      client.createBucket(new CreateBucketRequest(properties.name, properties.region))
    }
    else {
      logger.info(s"bucket with name ${properties.name} already exists")
    }
  }

  /**
    *
    * uploads a file to the bucket
    *
    * @param file
    */
  override def store(file: File) = if (properties.timeOfLife == 0) {
    client.putObject(properties.name, file.getName, file)
  } else {
    client.putObject(properties.name, file.getName, file).setExpirationTime(new Date(System.currentTimeMillis() + properties.timeOfLife))
  }


  /**
    * returns the related resource or none
    *
    * @param name
    * @return
    */
  override def load(name: String): Option[InputStream] = Option(client.getObject(properties.name, name).getObjectContent)

  /**
    * does the given resource exists
    *
    * @param name
    * @return
    */
  override def exists(name: String): Boolean = client.doesObjectExist(properties.name, name)
}

@Profile(Array("carrot.resource.store.bucket"))
@ConfigurationProperties(prefix = "carrot.resource.store.bucket")
@Component
class BucketStorageConfigurationProperties {

  @BeanProperty
  var name: String = "carrot-data"

  var region: String = Regions.US_EAST_2.getName

  @BeanProperty
  val timeOfLife: Long = 0
}

@EnableConfigurationProperties
@Configuration
@ComponentScan
@Profile(Array("carrot.resource.store.bucket"))
class BucketStorageConfiguration {

  @Bean
  def client(amazonAWSCredentials: AWSCredentialsProvider, properties: BucketStorageConfigurationProperties): AmazonS3 = AmazonS3ClientBuilder.standard.withRegion(properties.region).withCredentials(amazonAWSCredentials).build
}