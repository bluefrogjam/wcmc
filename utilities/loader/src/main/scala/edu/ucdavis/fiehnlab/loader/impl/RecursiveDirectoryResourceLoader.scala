package edu.ucdavis.fiehnlab.loader.impl

import java.io.{File, FileInputStream, InputStream}

import edu.ucdavis.fiehnlab.loader.{LocalLoader, ResourceLoader}
import org.springframework.beans.factory.annotation.Autowired

/**
  * searches directories recursivly to find resources and returns the input stream to them, if found
  */
class RecursiveDirectoryResourceLoader @Autowired()(directory: File) extends LocalLoader {
  logger.debug(s"lookup folder: ${directory.getAbsolutePath}")

  if(!directory.exists()){
    directory.mkdirs()
  }
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
    logger.info(s"looking for ${name} in ${dir.getAbsolutePath}")
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

  override def toString = s"RecursiveDirectoryResourceLoader(directory: ${directory.getAbsolutePath})"

  override def exists(name: String): Boolean = walkTree(directory).exists(p => p.getAbsolutePath.contains(name))

  private final def walkTree(file: File): Iterable[File] = {
    val children = new Iterable[File] {
      def iterator: Iterator[File] = if (file.isDirectory) file.listFiles.iterator else Iterator.empty
    }
    Seq(file) ++: children.flatMap(walkTree)
  }
}
