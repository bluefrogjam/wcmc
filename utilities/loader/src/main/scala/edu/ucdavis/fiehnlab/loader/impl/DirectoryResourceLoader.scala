package edu.ucdavis.fiehnlab.loader.impl

import java.io.{File, FileInputStream, InputStream}
import java.util.zip.ZipInputStream

import edu.ucdavis.fiehnlab.loader.{LocalLoader, ResourceLoader}
import org.springframework.beans.factory.annotation.Autowired
import org.zeroturnaround.zip.ZipUtil

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

    if (toLoad.exists()) {
      if(toLoad.isDirectory) {
        val zipFile = if(name.contains("/")) File.createTempFile("tmp",s"${cleanName(name.substring(name.lastIndexOf("/")))}.zip") else File.createTempFile("tmp",s"${cleanName(name)}.zip")
        ZipUtil.pack(toLoad, zipFile)
        Some(new ZipInputStream(new FileInputStream(zipFile)))
      } else {
        Some(new FileInputStream(toLoad))
      }
    } else {
      None
    }
  }

  override def exists(name: String): Boolean = {
    new File(directory, cleanName(name)).exists()
  }

  override def isDirectory(name: String): Boolean = {
    new File(directory, cleanName(name)).isDirectory
  }

  override def isFile(name: String): Boolean = {
    new File(directory, cleanName(name)).isFile
  }

  private def cleanName(name: String): String = {
    if(name.startsWith("/")) { name.substring(1) } else { name }
  }
}
