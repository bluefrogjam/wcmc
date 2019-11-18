package edu.ucdavis.fiehnlab.loader.impl

import java.io.{File, FileInputStream, InputStream}

import edu.ucdavis.fiehnlab.loader.LocalLoader

/**
  * Created by diego on 7/13/2017.
  */
class TempResourceLoader extends LocalLoader {
  /**
    * returns the related resource or none
    *
    * @param name
    * @return
    */
  override def load(name: String): Option[InputStream] = {
    val file = new File(getTempFolder, name)

    if (exists(file.getAbsolutePath)) {
	    logger.debug(s"\tResource found in temp ${file.getAbsolutePath}")
      Option(new FileInputStream(file))
    } else {
      logger.debug(s"\tResource not found in temp: ${file.getAbsolutePath}")
      None
    }
  }

  private def getTempFolder = {
    new File(System.getProperty("java.io.tmpdir"))
  }

  /**
    * will load the resource as file, by utilizing a TEMP directory
    * should be avoided due to uneccesaery performance overhead, but some tools
    * sadly require files and can't handle streams
    *
    * @param name
    * @return
    */
  override def loadAsFile(name: String): Option[File] = {
    val file = new File(getTempFolder, name)

    if (file.exists()) Some(file)
    else None
  }

  /**
    * does the given resource exists
    *
    * @param name
    * @return
    */
  override def exists(name: String): Boolean = {
    new File(new File(System.getProperty("java.io.tmpdir")),name).exists()
  }

  override def priority: Int = -100

  /**
   * internal storage source path
   */
  override def getSource: String = System.getProperty("java.io.tmpdir")
}
