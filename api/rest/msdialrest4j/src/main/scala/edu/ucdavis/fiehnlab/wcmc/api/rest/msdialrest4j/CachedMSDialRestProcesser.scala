package edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j

import java.io.File

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * utilizes our FServer to cache processing results somewhere on the file system o
  */
@Component
class CachedMSDialRestProcesser extends MSDialRestProcessor {

  @Autowired
  val fServ4jClient:FServ4jClient = null

  @Autowired
  val resourceLoader:ResourceLoader = null
  /**
    * processes the input file and includes caching support
    * if enabled
    *
    * @param input
    * @return
    */
  override def process(input: File): File = {

    val newFile = s"${input.getName}.processed"

    if(!resourceLoader.exists(newFile)){
      logger.info(s"file: ${input.getName} requires processing and will be stored as ${newFile}")
      fServ4jClient.upload(super.process(input),name = Some(newFile))
    }

    resourceLoader.loadAsFile(newFile).get
  }
}
