package edu.ucdavis.fiehnlab.ms.carrot.cloud.bucket

import java.io.{File, InputStream}
import java.util.Date

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.s3.model.CreateBucketRequest
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import edu.ucdavis.fiehnlab.loader.{ResourceLoader, ResourceStorage}
import edu.ucdavis.fiehnlab.ms.carrot.cloud.aws.ASWConfigurationProperties
import javax.annotation.PostConstruct
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.{ConfigurationProperties, EnableConfigurationProperties}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}
import org.springframework.stereotype.Component

import scala.beans.BeanProperty

/**
  * amazon S3 bucket storage access
  */
@Component
@Profile(Array("carrot.resource.store.bucket"))
class BucketStorage @Autowired()(client: AmazonS3, properties: BucketStorageConfigurationProperties, awsProperties: ASWConfigurationProperties) extends ResourceStorage with Logging {

  @PostConstruct
  def init() = {
    if (!client.doesBucketExist(properties.name)) {
      logger.info(s"creating new bucket with name: ${properties.name}")
      client.createBucket(new CreateBucketRequest(properties.name, awsProperties.region))
    }
    else {
      logger.info(s"bucket with name ${properties.name} already exists in region ${awsProperties.region}")
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
    * deletes the given file from the storage
    *
    * @param name
    */
  override def delete(name: String): Unit = {
    client.deleteObject(properties.name, name)
  }
}

@Component
@Profile(Array("carrot.resource.loader.bucket"))
class BucketLoader @Autowired()(client: AmazonS3, properties: BucketStorageConfigurationProperties, awsProperties: ASWConfigurationProperties) extends ResourceLoader {

  @PostConstruct
  def init() = {
    if (!client.doesBucketExist(properties.name)) {
      logger.info(s"creating new bucket with name: ${properties.name}")
      client.createBucket(new CreateBucketRequest(properties.name, awsProperties.region))
    }
    else {
      logger.info(s"bucket with name ${properties.name} already exists")
    }
  }

  /**
    * returns the related resource or none
    *
    * @param name
    * @return
    */
  override def load(name: String): Option[InputStream] = {
    if (exists(name)) {
      logger.info(s"\tLoading targets from (${properties.name}, $name)")
      Option(client.getObject(properties.name, name).getObjectContent)
    } else {
      None
    }
  }

  /**
    * does the given resource exists
    *
    * @param name
    * @return
    */
  override def exists(name: String): Boolean = client.doesObjectExist(properties.name, name)

  override def toString: String = super.toString.concat(s"[properties: $properties]")
}

@Profile(Array("carrot.resource.store.bucket", "carrot.resource.loader.bucket"))
@ConfigurationProperties(prefix = "wcmc.workflow.resource.store.bucket")
@Component
class BucketStorageConfigurationProperties {

  @BeanProperty
  var name: String = "data-carrot"

  @BeanProperty
  val timeOfLife: Long = 0
}

@EnableConfigurationProperties
@Configuration
@ComponentScan
@Profile(Array("carrot.resource.store.bucket", "carrot.resource.loader.bucket"))
class BucketStorageConfiguration {

  @Bean
  def client(amazonAWSCredentials: AWSCredentialsProvider, properties: BucketStorageConfigurationProperties, awsProperties: ASWConfigurationProperties): AmazonS3 = AmazonS3ClientBuilder.standard.withRegion(awsProperties.region).withCredentials(amazonAWSCredentials).build
}
