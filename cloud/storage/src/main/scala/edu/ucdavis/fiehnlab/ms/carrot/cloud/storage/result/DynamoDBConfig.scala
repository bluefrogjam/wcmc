package edu.ucdavis.fiehnlab.ms.carrot.cloud.storage.result

@Configuration
@ComponentScan
@Profile(Array("carrot.store.result.dynamo"))
@EnableDynamoDBRepositories(basePackageClasses = Array(classOf[DynamoSampleRepository]))
class DynamoDBConfig {

  @Value("${aws.region:us-west-2}")
  val region = ""

  @Bean
  def amazonDynamoDB(amazonAWSCredentials: AWSCredentialsProvider): AmazonDynamoDB = {
    AmazonDynamoDBClientBuilder.standard.withRegion(region)build()
  }

}
