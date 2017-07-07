package edu.ucdavis.fiehnlab.ms.carrot.core.io

import java.io.File
import javax.validation.Valid
import javax.validation.constraints.NotNull

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.scanner.SampleFactory
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

import scala.beans.BeanProperty
import scala.collection.JavaConverters._

/**
  * Created by wohlg on 7/11/2016.
  */
class LocalDirectorySampleLoader @Autowired()(val properties: LocalDirectorySampleLoaderProperties) extends SampleLoader with LazyLogging {

  /**
    * loads a sample
    *
    * @param name
    * @return
    */
  override def loadSample(name: String): Option[Sample] = {
    val result = properties.directories.asScala.collectFirst {
      case dir: String if buildFile(dir, name).exists() => SampleFactory.build(buildFile(dir, name))
    }

	  if(result.isEmpty) {
      logger.warn(s"no sample ${name} found in any of the defined directories: ${properties.directories}")
      None
    }
    else {
      result
    }
  }

  /**
    * creates an instance of a file for us
    * @param dir
    * @param name
    * @return
    */
  def buildFile(dir: String, name: String): File = new File(new File(dir), name)

  /**
    * checks if the sample exist
    *
    * @return
    */
  override def sampleExists(name: String): Boolean = {
    val result = properties.directories.asScala.collectFirst {
      case dir: String if buildFile(dir, name).exists() => true
    }

    result.isDefined
  }
}

@Component
@ConfigurationProperties(prefix = "storage")
class LocalDirectorySampleLoaderProperties {

  @Valid
  @NotNull(message = "please provide a directory where your data are located using --storage.directories=<character> (separate directories by ',') ")
  @BeanProperty
  var directories: java.util.List[String] = ("./" :: List()).asJava
}
