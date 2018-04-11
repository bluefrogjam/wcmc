package edu.ucdavis.fiehnlab.ms.carrot.cloud.aws

import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain}
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}

@Configuration
@EnableConfigurationProperties
@ComponentScan
class AWSConfiguration {

  /**
    * provides us with a default authentication provider chain for all AWS related
    * beans
    * @return
    */
  @Bean
  def provider: AWSCredentialsProvider = new DefaultAWSCredentialsProviderChain

}
