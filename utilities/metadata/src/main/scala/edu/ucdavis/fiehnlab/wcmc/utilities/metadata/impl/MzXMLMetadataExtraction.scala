package edu.ucdavis.fiehnlab.wcmc.utilities.metadata.impl

import java.io.File

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.wcmc.utilities.metadata.api.MetadataExtraction
import edu.ucdavis.fiehnlab.wcmc.utilities.metadata.db.{FileMetadata, FileMetadataRepository, FileMetadataService}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.xml.XML


@Component
class MzXMLMetadataExtraction extends MetadataExtraction with Logging {

  @Autowired
  val mdRepo: FileMetadataRepository = null

  override def getMetadata(file: File): Option[FileMetadata] = {
    val data = XML.loadFile(file)

    if (isValidFormat(file)) {
      val md = new FileMetadataService().getFileMetadata(file.getName, data)
      mdRepo.save(md)
      Option(md)
    } else {
      None
    }
  }

  override def format = "mzXML"
}
