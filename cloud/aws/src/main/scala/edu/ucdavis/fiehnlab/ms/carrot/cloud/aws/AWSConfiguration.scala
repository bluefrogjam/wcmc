package edu.ucdavis.fiehnlab.ms.carrot.cloud.aws

import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain, EnvironmentVariableCredentialsProvider}
import com.amazonaws.regions.Regions
import javax.validation.constraints.{NotBlank, NotNull}
import org.springframework.boot.context.properties.{ConfigurationProperties, EnableConfigurationProperties}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.stereotype.Component

import scala.beans.BeanProperty

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
//  def provider: AWSCredentialsProvider = new EnvironmentVariableCredentialsProvider
  def provider: AWSCredentialsProvider = new DefaultAWSCredentialsProviderChain

}

@ConfigurationProperties(prefix = "wcmc.config.aws")
@Component
class ASWConfigurationProperties{

  /**
    * region for deployments, oregon is cheap and so we choose it
    */
  @BeanProperty
  @NotBlank
  @NotNull
  var region:String = Regions.US_WEST_2.getName

}
