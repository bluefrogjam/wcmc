package edu.ucdavis.fiehnlab.ms.carrot.core

import java.io.File

import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j.MSDialRestProcessor
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}

/**
  * Created by wohlgemuth on 11/29/16.
  */
@Configuration
@ComponentScan(basePackageClasses = Array(classOf[MSDialRestProcessor]))
class LoadersConfiguration {


  /**
    * below there will be all different directory loaders from the different workstations we are working on
    * smarter would be to use spring profiles
    *
    * @return
    */
  @Bean
  def resourceLoaderSrc: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("src"))

}
