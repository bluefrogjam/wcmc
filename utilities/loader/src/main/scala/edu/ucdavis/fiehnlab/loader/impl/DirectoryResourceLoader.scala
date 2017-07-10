package edu.ucdavis.fiehnlab.loader.impl

import java.io.{File, FileInputStream, InputStream}

import edu.ucdavis.fiehnlab.loader.{LocalLoader, ResourceLoader}
import org.springframework.beans.factory.annotation.Autowired

/**
  * Attempts to load a file directly from a directory
  */
class DirectoryResourceLoader @Autowired()(val directory: File) extends LocalLoader {
  /**
    * returns the related resource or none
    *
    * @param name
    * @return
    */
  override def load(name: String): Option[InputStream] = {
    val toLoad = new File(directory, name)

    if (toLoad.exists()) Some(new FileInputStream(toLoad)) else None
  }

  override def exists(name: String): Boolean = {
    new File(directory, cleanName(name)).exists()
  }

  private def cleanName(name: String): String = {
    if(name.startsWith("/")) { name.substring(1) } else { name }
  }
}