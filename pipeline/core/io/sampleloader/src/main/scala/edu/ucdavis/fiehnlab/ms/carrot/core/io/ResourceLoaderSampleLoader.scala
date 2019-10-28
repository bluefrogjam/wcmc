package edu.ucdavis.fiehnlab.ms.carrot.core.io

import java.io.{File, FileInputStream}

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.leco.LecoSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk.MSDKSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import io.github.msdk.MSDKException
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.{ComponentScan, Configuration, Profile}
import org.springframework.stereotype.Component

/**
  * utilizes the new resource loader api
  * which allows us to load files remotely
  * or from other locations
  */
@Component
@Profile(Array("!carrot.loader.autoconvert"))
class ResourceLoaderSampleLoader @Autowired()(resourceLoader: ResourceLoader) extends SampleLoader with Logging {

  /**
    * loads a sample
    *
    * @param name
    * @return
    */
  override def loadSample(name: String): Option[_ <: Sample] = {
    logger.info(s"looking for sample: ${name} with ${resourceLoader}")
    try {
      val fileOption = resourceLoader.loadAsFile(name)
      convertFileToSample(name, fileOption)
    }
    catch {
      case e: MSDKException =>
        //most likely a download failure, retru
        val fileOption = resourceLoader.loadAsFile(name)
        convertFileToSample(name, fileOption)
    }
  }

  protected def convertFileToSample(name: String, fileOption: Option[File]): Option[_ <: Sample] = {

    val begin = System.currentTimeMillis()
    try {
      if (fileOption.isDefined) {
        logger.debug(s"converting ${fileOption.get.getName} to sample")
        val file = fileOption.get
        if (file.getName.toLowerCase.matches(".*\\.txt")) {
          Some(new LecoSample(new FileInputStream(file), name))
        }
        else {
          Some(MSDKSample(name, file))
        }
      }
      else {
        None
      }
    }
    finally {
      val duration = System.currentTimeMillis() - begin

      logger.debug(s"duration to convert file was ${duration} ms")
    }
  }

  /**
    * checks if the sample exist
    *
    * @param name
    * @return
    */
  @Cacheable(value = Array("resource-exist-sample"), key = "#name")
  override def sampleExists(name: String): Boolean = {
    resourceLoader.exists(name)
  }
}

@Configuration
@ComponentScan
class SampleLoaderConfiguration {

}