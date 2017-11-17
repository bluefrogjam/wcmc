package edu.ucdavis.fiehnlab.wcmc.utilities.metadata.db

import com.typesafe.scalalogging.LazyLogging
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.{Component, Repository}

import scala.annotation.meta.field
import scala.xml.{Elem, NodeSeq}

@Repository
trait FileMetadataRepository extends MongoRepository[FileMetadata, String] {
  def findByFilename(filename: String): FileMetadata
}

@Document(collection = "metadata")
case class FileMetadata(
                           @(Id@field)
                           id: String,
                           filename: String,
                           manufacturer: String,
                           model: String,
                           ionization: String,
                           massAnalyzer: String,
                           detector: String,
                           software: String) {}

@Component
class FileMetadataService() extends LazyLogging {
  def getFileMetadata(filename: String, data: Elem): FileMetadata = {
    val manufacturer: NodeSeq = data \ "msRun" \ "msInstrument" \ "msManufacturer" \ "@value"
    val model: NodeSeq = data \ "msRun" \ "msInstrument" \ "msModel" \ "@value"
    val ionization: NodeSeq = data \ "msRun" \ "msInstrument" \ "msIonisation" \ "@value"
    val massAnalyzer: NodeSeq = data \ "msRun" \ "msInstrument" \ "msMassAnalyzer" \ "@value"
    val detector: NodeSeq = data \ "msRun" \ "msInstrument" \ "msDetector" \ "@value"
    val software: NodeSeq = data \ "msRun" \ "msInstrument" \ "software" \ "@name"

    FileMetadata(null, filename, manufacturer.text, model.text, ionization.text, massAnalyzer.text, detector.text, software.text)
  }
}
