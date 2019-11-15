package edu.ucdavis.fiehnlab.loader

import java.io.{File, InputStream}
import java.nio.file.{Files, StandardCopyOption}


/**
  * provides a simple way to upload a file as a resource of our choosing
  */
trait ResourceStorage {

  /**
    * stores a stream
    *
    * @param inputStream
    * @param name
    */
  def store(inputStream: InputStream, name: String): Unit = {
    val dir = Files.createTempDirectory("lc-binbase-store")
    val out = new File(dir.toFile, name)
    out.deleteOnExit()
    Files.copy(inputStream, out.toPath, StandardCopyOption.REPLACE_EXISTING)
    store(out)
    inputStream.close()
  }

  /**
    * store the given file
    *
    * @param file
    */

  def store(file: File)

  /**
    * deletes the given file from the storage
    *
    * @param name
    */
  def delete(name: String)
}

trait OutputStorage extends ResourceStorage

trait InputStorage extends ResourceStorage