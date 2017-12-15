package edu.ucdavis.fiehnlab.ms.carrot.core.io

import java.io.File

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable

/**
  * this sample loader supports, the automatic conversion of data
  * on demand to satisfy the usage of the local client
  */
class ConversionAwareSampleLoader @Autowired()(dataForm: DataFormerClient, resourceLoader: ResourceLoader) extends ResourceLoaderSampleLoader(resourceLoader) {

  /**
    * this are the extensions the dataformer client supports natively
    */
  val supportedInputExtensionForDataForm: Array[String] = Array("d.zip", "raw", "wiff")

  /**
    * these are the extensions the data form client can convert too
    */
  val supportOutputExtensionForDataForm: Array[String] = Array("abf", "mzML", "mzXML")

  /**
    * loads a sample as an option, so that we can evaluate it we have it or not, without an exception
    *
    * @param name
    * @return
    */
  override def loadSample(name: String): Option[_ <: Sample] = {
    if (super.sampleExists(name)) {
      super.loadSample(name)
    }
    else {
      convertFileToSample(name, convertFile(name))
    }
  }

  /**
    * checks if the sample exist
    *
    * @param name
    * @return
    */
  override def sampleExists(name: String): Boolean = {
    if (super.sampleExists(name)) {
      true
    }
    else {
      //start the conversion process
      convertFile(name).isDefined
    }
  }

  /**
    * does the actual conversion for us
    *
    * @param name
    */
  @Cacheable
  private def convertFile(name: String): Option[File] = {
    logger.info(s"attempting to convert to: ${name}")
    val split = name.split("\\.")
    val fileName = split.head
    val extension = split.last


    val result = supportedInputExtensionForDataForm.collectFirst {

      case x: String if {
        logger.info(s"checking for: ${fileName}.${x}")
        resourceLoader.exists(s"${fileName}.${x}")
      } =>
        val fileToConvert = s"${fileName}.${x}"
        logger.info(s"found rawdata file: ${fileToConvert}")
        dataForm.convert(fileToConvert, extension)
    }

    logger.info(s"conversion successful: ${result.isDefined}")

    if(result.isDefined){
      result.get
    }
    else{
      None
    }
  }
}
