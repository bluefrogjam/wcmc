package edu.ucdavis.fiehnlab.wcmc.server.rdmonitor.api

import java.io.File

trait FileEvent {
  val file: File
  val refTimeStamp: Long
}

case class NewFileEvent(file: File, refTimeStamp: Long) extends FileEvent

trait FileEventListener {
  def foundFile(event: FileEvent): Unit
}
