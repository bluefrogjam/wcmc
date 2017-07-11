package edu.ucdavis.fiehnlab.ms.carrot.core

import java.io.File

import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.io.ResourceLoaderSampleLoader
import edu.ucdavis.fiehnlab.wcms.api.rest.fserv4j.FServ4jClient
import edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j.MSDialRestProcessor
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.web.client.RestTemplate

/**
  * Created by wohlgemuth on 11/29/16.
  */
@Configuration
@ComponentScan(basePackageClasses = Array(classOf[MSDialRestProcessor]))
class LoadersConfiguration {


  /**
    * below there will be all different directory loaders from the different workstations we are working on
    * smarter would be to use spring profiles
    * @return
    */
  @Bean
  def resourceLoaderSrc: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("src"))

  @Bean
  def resourceSampleLoader(resourceLoader: DelegatingResourceLoader): ResourceLoaderSampleLoader = new ResourceLoaderSampleLoader(resourceLoader)

  @Bean
  def remoteLoader:FServ4jClient = new FServ4jClient

  @Bean
  def restTemplate:RestTemplate = new RestTemplate()
}
