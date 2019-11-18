package edu.ucdavis.fiehnlab.ms.carrot.cloud.bucket

import java.io.File
import java.util.Date

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CreateBucketRequest
import edu.ucdavis.fiehnlab.loader.{InputStorage, OutputStorage, ResourceStorage}
import edu.ucdavis.fiehnlab.ms.carrot.cloud.aws.ASWConfigurationProperties
import javax.annotation.PostConstruct
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired

/**
  * amazon S3 bucket storage access
  */
abstract class BucketStorage @Autowired()(client: AmazonS3, properties: BucketStorageConfigurationProperties, awsProperties: ASWConfigurationProperties) extends ResourceStorage with Logging {

  @PostConstruct
  def init(): Any = {
    if (!client.doesBucketExist(properties.name)) {
      logger.info(s"creating new bucket with name: $getDestination")
      client.createBucket(new CreateBucketRequest(getDestination, awsProperties.region))
    }
    else {
      logger.info(s"bucket with name $getDestination already exists in region ${awsProperties.region}")
    }
  }

  override def store(file: File): Unit = if (properties.timeOfLife == 0) {
    client.putObject(getDestination, file.getName, file)
  } else {
    client.putObject(getDestination, file.getName, file).setExpirationTime(new Date(System.currentTimeMillis() + properties.timeOfLife))
  }

  override def delete(name: String): Unit = {
    client.deleteObject(getDestination, name)
  }

  override def exists(filename: String): Boolean = {
    client.doesObjectExist(getDestination, filename)
  }

  override def toString: String = super.toString.concat(s"[properties: $properties]")

}

class InputBucketStorage @Autowired()(client: AmazonS3, properties: BucketStorageConfigurationProperties, awsProperties: ASWConfigurationProperties) extends BucketStorage(client, properties, awsProperties) with InputStorage {

  override def getDestination: String = properties.name
}


class OutputBucketStorage @Autowired()(client: AmazonS3, properties: BucketStorageConfigurationProperties, awsProperties: ASWConfigurationProperties) extends BucketStorage(client, properties, awsProperties) with OutputStorage {

  override def getDestination: String = properties.result
}
