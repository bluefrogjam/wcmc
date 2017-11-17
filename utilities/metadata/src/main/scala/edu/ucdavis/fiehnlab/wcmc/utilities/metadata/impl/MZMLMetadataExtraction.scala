package edu.ucdavis.fiehnlab.wcmc.utilities.metadata.impl

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.utilities.metadata.api.MetadataExtraction
import edu.ucdavis.fiehnlab.wcmc.utilities.metadata.db.{FileMetadata, FileMetadataService}
import org.springframework.stereotype.Component

import scala.xml._

@Component
class MZMLMetadataExtraction extends MetadataExtraction with LazyLogging {

  override def getMetadata(file: File): Option[FileMetadata] = {
    if (!isValidFormat(file)) {
      None
    } else {
      val xmldata = XML.loadFile(file)
      val trueRoot = xmldata \\ "indexedmzML" \ "mzML"
      val refParamGrp = trueRoot \\ "referenceableParamGroupList" \ "referenceableParamGroup"
      val model = refParamGrp \\ "userParam" \ "@name"
      val value = refParamGrp \\ "userParam" \ "@value"

      val softwares = trueRoot \\ "softwareList" \ "software"


      logger.debug(model.text)
      logger.debug(value.text)
      logger.debug(softwares.map(s => Map("name" -> s \\ "@id", "ver" -> s \\ "@version")).mkString("\n"))

      Option(new FileMetadataService().getFileMetadata(file.getName, xmldata))
    }
  }

  override def format = "mzML"
}
