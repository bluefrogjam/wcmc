package edu.ucdavis.fiehnlab.loader

import java.io.File

/**
  * provides a simple way to upload a file as a resource of our choosing
  */
trait ResourceStorage {

  /**
    * store the given file
    * @param file
    */
  def store(file: File)
}
