package edu.ucdavis.fiehnlab.ms.carrot.core.io

import java.io.{File, FileInputStream}

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.agilent.AgilentSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.leco.LecoSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk.MSDKSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable

/**
  * utilizes the new resource loader api
  * which allows us to load files remotely
  * or from other locations
  */
class ResourceLoaderSampleLoader @Autowired()(resourceLoader: ResourceLoader) extends SampleLoader with Logging {


  @Autowired
  val dataFormerClient: DataFormerClient = null

  /**
    * loads a sample
    *
    * @param name
    * @return
    */
  override def loadSample(name: String): Option[_ <: Sample] = {
    logger.info(s"looking for sample: ${name} with ${resourceLoader}")
    val fileOption = resourceLoader.loadAsFile(name)
    convertFileToSample(name, fileOption)
  }

  protected def convertFileToSample(name: String, fileOption: Option[File]): Option[_ <: Sample] = {

    val begin = System.currentTimeMillis()
    try {
      if (fileOption.isDefined) {
        logger.debug(s"converting ${fileOption.get.getName} to sample")
        val file = fileOption.get
        if (file.getName.toLowerCase.matches(".*\\.d(\\.zip)?")) {
          Some(new AgilentSample(file.getName, file, dataFormerClient))
        }
        else if (file.getName.toLowerCase.matches(".*\\.txt")) {
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
