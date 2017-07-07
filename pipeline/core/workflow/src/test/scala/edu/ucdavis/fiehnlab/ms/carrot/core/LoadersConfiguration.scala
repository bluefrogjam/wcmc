package edu.ucdavis.fiehnlab.ms.carrot.core

import java.io.File
import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import org.springframework.context.annotation.{Bean, Configuration}

/**
  * Created by wohlgemuth on 11/29/16.
  */
@Configuration
class LoadersConfiguration {


  /**
    * below there will be all different directory loaders from the different workstations we are working on
    * smarter would be to use spring profiles
    * @return
    */

  @Bean
  def directoryResourceLoaderGert:RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File(new File(System.getProperty("user.home")),"Google Drive"))

  @Bean
  def resourceLoaderSrc: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("src"))

  @Bean
  def resourceLoaderDiegoWorkstation: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("G:\\Data\\carrot\\P20-lipids\\carrot"))

  @Bean
  def resourceLoaderGertWorkstation: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("G:\\gdrive\\testdata\\carrot\\qc\\raw"))

  @Bean
  def resourceSampleLoader(resourceLoader: DelegatingResourceLoader): ResourceLoaderSampleLoader = new ResourceLoaderSampleLoader(resourceLoader)

}
