package edu.ucdavis.fiehnlab.ms.carrot.core.api.metadata

import java.util

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import edu.ucdavis.fiehnlab.wcmc.utilities.metadata.api.MetadataExtraction
import edu.ucdavis.fiehnlab.wcmc.utilities.metadata.db.{FileMetadata, FileMetadataRepository}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

@Component
class MetaDataResolver {

  @Autowired(required = false)
  val extractors: java.util.List[MetadataExtraction] = new util.ArrayList[MetadataExtraction]()

  @Autowired
  val loader: ResourceLoader = null

  @Autowired(required = false)
  val metadataRepository: FileMetadataRepository = null

  val validExtensions: Array[String] = Array("mzML", "mzml")

  /**
    * resolve metadata for this sample
    *
    * @param sample
    * @return
    */
  def resolve(sample: Sample): Option[FileMetadata] = {

    //check if the repository has our data
    val result = validExtensions.collectFirst {
      case x if metadataRepository != null && metadataRepository.findByFilename(s"${sample.name}.${x}") != null => metadataRepository.findByFilename(s"${sample.name}.${x}")
    }

    if (result.isDefined) {
      result
    }

    //if not try to extract the metadata from the resource loader
    else {
      val extractor = extractors.asScala.find(_.format.equalsIgnoreCase(sample.extension))

      validExtensions.collectFirst {
        case x if loader.exists(s"${sample.name}.${x}") && extractor.isDefined =>
          extractor.get.getMetadata(loader.loadAsFile(s"${sample.name}.${x}").get)
      }.flatten

    }
  }
}
