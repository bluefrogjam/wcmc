package edu.ucdavis.fiehnlab.loader.impl

import java.io.{File, FileInputStream, InputStream}

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import org.springframework.beans.factory.annotation.Autowired

/**
  * searches directories recursivly to find resources and returns the input stream to them, if found
  */
class RecursiveDirectoryResourceLoader @Autowired()(directory: File) extends ResourceLoader {
  /**
    * returns the related resource or none
    *
    * @param name
    * @return
    */
  override def load(name: String): Option[InputStream] = {
    if(directory.exists()) {
      file(directory, name)
    }
    else{
      None
    }
  }

  /**
    * recursively load files
    *
    * @param dir
    * @param name
    * @return
    */
  def file(dir: File, name: String): Option[InputStream] = {
    val toLoad = new File(dir, name)

    if (toLoad.exists()) {
      Some(new FileInputStream(toLoad))
    }
    else {
      val sub = dir.listFiles().filter(_.isDirectory).collect {
        case currentDir: File => file(currentDir, name)
      }.filter(_.isDefined)

      if (sub.isEmpty) {
        None
      }
      else {
        sub.head
      }
    }
  }

  override def toString = s"RecursiveDirectoryResourceLoader(directory: ${directory})"
}
