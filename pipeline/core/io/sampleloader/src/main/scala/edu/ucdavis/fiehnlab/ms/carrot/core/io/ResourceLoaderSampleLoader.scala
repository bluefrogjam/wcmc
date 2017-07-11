package edu.ucdavis.fiehnlab.ms.carrot.core.io

import java.io.{BufferedInputStream, File}
import java.nio.file.Files.copy
import java.nio.file.Paths

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
  /**
    * loads a sample
    *
    * @param name
    * @return
    */
  override def loadSample(name: String): Option[Sample] = {

    val file = resourceLoader.load(name)

    if (file.isDefined) {
      val dir = new File(System.getProperty("java.io.tmpdir"))

      val output = new File(dir, name)

	    //store temporary
      val path = Paths.get(output.getAbsolutePath)

	    if (output.exists()) {
		    output.delete()
	    }
	    copy(new BufferedInputStream(file.get), path)

      //return
      Some(build(name,output))
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

  def build(name:String,file: File): Sample = {
    //    println(s"file: ${file}")
    if (file.getName.toLowerCase().matches(".*.txt[.gz]*")) {
      //leco
      null
    }
    else if (file.getName.toLowerCase().matches(".*.msdial[.gz]*")) {
      MSDialSample(name,file)
    }
    else if (file.getName.toLowerCase().matches(".*.abf")) {
      new ABFSample(name,file,client)
    }

    else {
      MSDKSample(name,file)

    }
  }
}
