package edu.ucdavis.fiehnlab.ms.carrot.core.io

import java.io.{BufferedInputStream, File}
import java.nio.file.Files.copy
import java.nio.file.Paths

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.scanner.SampleFactory
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import org.springframework.beans.factory.annotation.Autowired

/**
  * utilizes the new resource loader api
  * which allows us to load files remotely
  * or from other locations
  */
//@Component
class ResourceLoaderSampleLoader @Autowired()(resourceLoader: ResourceLoader) extends SampleLoader with LazyLogging {

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
      Some(SampleFactory.build(output))
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
    resourceLoader.fileExists(name)
  }
}
