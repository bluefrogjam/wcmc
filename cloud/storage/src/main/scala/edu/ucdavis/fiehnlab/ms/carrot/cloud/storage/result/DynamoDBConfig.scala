package edu.ucdavis.fiehnlab.ms.carrot.cloud.storage.result

import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBClientBuilder}
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}

@Configuration
@ComponentScan
@Profile(Array("carrot.store.result.dynamo"))
@EnableDynamoDBRepositories(basePackageClasses = Array(classOf[DynamoSampleRepository]))
class DynamoDBConfig {

  @Value("${aws.region:us-east-1}")
  val region = ""

  @Bean
  def amazonDynamoDB(amazonAWSCredentials: AWSCredentialsProvider): AmazonDynamoDB = {
    AmazonDynamoDBClientBuilder.standard.withRegion(region)build()
  }

}
