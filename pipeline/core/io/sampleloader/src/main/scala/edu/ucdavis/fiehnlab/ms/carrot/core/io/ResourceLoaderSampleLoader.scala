package edu.ucdavis.fiehnlab.ms.carrot.core.io

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.abf.ABFSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial.MSDialSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk.MSDKSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j.MSDialRestProcessor
import org.springframework.beans.factory.annotation.Autowired

/**
  * utilizes the new resource loader api
  * which allows us to load files remotely
  * or from other locations
  */
class ResourceLoaderSampleLoader @Autowired()(resourceLoader: ResourceLoader) extends SampleLoader with LazyLogging {

  @Autowired
  val client: MSDialRestProcessor = null

  logger.info(s"using loader: ${resourceLoader}")

  /**
    * loads a sample
    *
    * @param name
    * @return
    */
  override def loadSample(name: String): Option[Sample] = {
    logger.debug(s"looking for sample: ${name}")
    val fileOption = resourceLoader.loadAsFile(name)

    if (fileOption.isDefined) {
      val file = fileOption.get
      if (file.getName.toLowerCase().matches(".*\\.txt(?:.gz)?")) { // .*.txt[.gz]*  can catch invalid files (blahtxt.gz)
        //leco
        None
      }
      else if (file.getName.toLowerCase().matches(".*\\.msdial(?:.gz)?")) { // .*.msdial[.gz]*  same issue as above (blahmsdial.gz  and blah.msdial. | blah.msdial.gz.)
        Some(MSDialSample(name, file))
      }
      else if (file.getName.toLowerCase().matches(".*\\.abf")) { // .*.abf can catch files that end in '.' like blah.abf.
        Some(new ABFSample(name, file, client))
      }
      else {
        Some(MSDKSample(name, file))
      }
    }
    else {
      None
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
