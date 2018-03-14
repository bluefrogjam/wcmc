package edu.ucdavis.fiehnlab.ms.carrot.core.io

import java.io.{File, IOException}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.abf.ABFSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.agilent.AgilentSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial.MSDialSampleV2
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk.MSDKSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient
import edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j.MSDialRestProcessor
import org.springframework.beans.factory.annotation.Autowired

/**
  * utilizes the new resource loader api
  * which allows us to load files remotely
  * or from other locations
  */
class ResourceLoaderSampleLoader @Autowired()(resourceLoader: ResourceLoader) extends SampleLoader with LazyLogging {

  @Autowired
  val client: MSDialRestProcessor = null

  @Autowired
  val dataFormerClient: DataFormerClient = null

  /**
    * loads a sample
    *
    * @param name
    * @return
    */
  override def loadSample(name: String): Option[_ <: Sample] = {
    logger.debug(s"looking for sample: ${name} with ${resourceLoader}")
    val fileOption = resourceLoader.loadAsFile(name)

    convertFileToSample(name, fileOption)
  }

  protected def convertFileToSample(name: String, fileOption: Option[File]): Option[_ <: Sample] = {

    val begin = System.currentTimeMillis()
    try {
      if (fileOption.isDefined) {
        logger.debug(s"converting ${fileOption.get.getName} to sample")
        val file = fileOption.get
        if (file.getName.toLowerCase().matches(".*\\.(msdial|processed)(?:.gz)?")) { // .*.msdial[.gz]*  same issue as above (blahmsdial.gz  and blah.msdial. | blah.msdial.gz.)
          Some(MSDialSampleV2(name, file))
        }
        else if (file.getName.toLowerCase().matches(".*\\.abf")) { // .*.abf can catch files that end in '.' like blah.abf.
          Some(new ABFSample(name, file, client))
        }
        else if (file.getName.toLowerCase.matches(".*\\.d.zip")) {
          Some(new AgilentSample(file.getName, file, dataFormerClient))
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
  override def sampleExists(name: String): Boolean = {
    resourceLoader.exists(name)
  }
}
