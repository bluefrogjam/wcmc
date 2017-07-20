package edu.ucdavis.fiehnlab.loader.impl

import java.io.{File, FileInputStream, InputStream}

import edu.ucdavis.fiehnlab.loader.LocalLoader
import org.springframework.stereotype.Component

/**
  * Created by diego on 7/13/2017.
  */
@Component
class TempResourceLoader extends LocalLoader {
  /**
    * returns the related resource or none
    *
    * @param name
    * @return
    */
  override def load(name: String): Option[InputStream] = {
    val dir = new File(System.getProperty("java.io.tmpdir"))
    val file = new File(dir, name)

    if (exists(file.getAbsolutePath)) {
	    logger.debug(s"\tResource found in temp ${file.getAbsolutePath}")
      Option(new FileInputStream(file))
    } else {
      logger.debug(s"\tResource not found in temp: ${file.getAbsolutePath}")
      None
    }
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
    val dir = new File(System.getProperty("java.io.tmpdir"))
    val file = new File(dir, name)

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
    new File(name).exists()
  }

  override def priority: Int = -100
}
