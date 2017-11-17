package edu.ucdavis.fiehnlab.wcmc.utilities.metadata.impl

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.utilities.metadata.api.MetadataExtraction
import edu.ucdavis.fiehnlab.wcmc.utilities.metadata.db.{FileMetadata, FileMetadataService}
import org.springframework.stereotype.Component

import scala.xml.XML


@Component
class MzXMLMetadataExtraction extends MetadataExtraction with LazyLogging {

  override def getMetadata(file: File): Option[FileMetadata] = {
    val data = XML.loadFile(file)

    if (isValidFormat(file)) {
      Option(new FileMetadataService().getFileMetadata(file.getName, data))
    } else {
      None
    }
  }

  override def format = "mzXML"
}
