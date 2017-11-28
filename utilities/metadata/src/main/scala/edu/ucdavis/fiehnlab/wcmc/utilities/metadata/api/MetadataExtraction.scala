package edu.ucdavis.fiehnlab.wcmc.utilities.metadata.api

import java.io.File

import edu.ucdavis.fiehnlab.wcmc.utilities.metadata.db.FileMetadata

trait MetadataExtraction {

  def getMetadata(file: File): Option[FileMetadata]

  def isValidFormat(file: File): Boolean = {
    file.getName.endsWith(format)
  }

  def format: String
}
