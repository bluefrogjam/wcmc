package edu.ucdavis.fiehnlab.ms.carrot.integration.io.convert

import java.io.File

import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.Message
import org.springframework.stereotype.Component

/**
  * Created by wohlgemuth on 3/13/17.
  */
@Component
class ZipActivator {

  @ServiceActivator
  def zip(directory: Message[File]): Message[File] = {

    val file:File = directory.getPayload
    val result:File = if (file.isDirectory) zipDir(file) else zipFile(file)

    MessageBuilder.withPayload(result).copyHeaders(directory.getHeaders).build()
  }

  /**
    * create a zip archive based on a single file
    *
    * @param file
    * @return
    */
  def zipFile(file: File): File = null

  /**
    * creates a zip archive based on the content of a directory
    *
    * @param file
    * @return
    */
  def zipDir(file: File): File = null

}
