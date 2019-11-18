package edu.ucdavis.fiehnlab.ms.carrot.cloud.bucket

import java.io.InputStream

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CreateBucketRequest
import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.cloud.aws.ASWConfigurationProperties
import javax.annotation.PostConstruct
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired


abstract class BucketLoader @Autowired()(client: AmazonS3, properties: BucketStorageConfigurationProperties, awsProperties: ASWConfigurationProperties) extends ResourceLoader with Logging {

  @PostConstruct
  def init(): Any = {
    if (!client.doesBucketExist(getSource)) {
      logger.info(s"creating new bucket with name: $getSource")
      client.createBucket(new CreateBucketRequest(getSource, awsProperties.region))
    }
    else {
      logger.info(s"bucket with name $getSource already exists")
    }
  }

  override def load(name: String): Option[InputStream] = {
    if (exists(name)) {
      logger.info(s"\tLoading targets from ($getSource, $name)")
      Option(client.getObject(getSource, name).getObjectContent)
    } else {
      None
    }
  }

  override def exists(name: String): Boolean = client.doesObjectExist(getSource, name)

  override def toString: String = super.toString.concat(s"[properties: $properties]")
}

class InputBucketLoader @Autowired()(client: AmazonS3, properties: BucketStorageConfigurationProperties, awsProperties: ASWConfigurationProperties) extends BucketLoader(client, properties, awsProperties) {

  override def getSource: String = properties.name
}

class OutputBucketLoader @Autowired()(client: AmazonS3, properties: BucketStorageConfigurationProperties, awsProperties: ASWConfigurationProperties) extends BucketLoader(client, properties, awsProperties) {

  override def getSource: String = properties.result
}
