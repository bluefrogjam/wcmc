package edu.ucdavis.fiehnlab.loader.impl

import java.io.{File, FileInputStream, InputStream}
import java.util.zip.ZipInputStream

import edu.ucdavis.fiehnlab.loader.LocalLoader
import org.springframework.beans.factory.annotation.Autowired
import org.zeroturnaround.zip.ZipUtil

/**
  * searches directories recursivly to find resources and returns the input stream to them, if found
  */
class RecursiveDirectoryResourceLoader @Autowired()(directory: File, override val priority: Int = 0) extends LocalLoader {

  if (!directory.exists()) {
    logger.info(s"making directory: ${directory.getAbsolutePath}")
    directory.mkdirs()
  }
  else {
    logger.debug(s"lookup folder is: ${directory.getAbsolutePath}")
  }

  /**
    * returns the related resource or none
    *
    * @param name
    * @return
    */
  override def load(name: String): Option[InputStream] = {
    val result = loadAsFile(name)

    if (result.isDefined) {
        Option(new FileInputStream(result.get))
    } else {
      None
    }
  }

  /**
    * @param name
    * @return
    */
  override def loadAsFile(name: String): Option[File] = {
    logger.debug(s"recursively load ${name} from ${directory.getAbsolutePath}")

    val file = find(name)
    if (file.isDefined) {
      if (file.get.isFile) {
        file
      } else if (file.get.isDirectory && file.get.getName.equals(name)) {
        val zipFile = if (name.contains("/")) File.createTempFile("tmp", s"${cleanName(name.substring(name.lastIndexOf("/")))}.zip") else File.createTempFile("tmp", s"${cleanName(name)}.zip")
        logger.debug(s"zipping ${file.get.getName}")
        ZipUtil.pack(file.get, zipFile)
        Some(zipFile)
      } else {
        None
      }
    } else {
      None
    }
    //		val files = walkTree(directory).filter(p => p.getAbsolutePath.endsWith(name) && p.isFile)
    //		files.headOption
  }

  override def toString = s"RecursiveDirectoryResourceLoader(directory: ${directory.getAbsolutePath})"

  override def exists(name: String): Boolean = walkTree(directory).exists(p => p.getAbsolutePath.endsWith(name))

  override def isDirectory(name: String): Boolean = {
    val file = find(name)
    if (file.isDefined) {
      file.get.isDirectory
    } else {
      false
    }
  }

  override def isFile(name: String): Boolean = {
    val file = find(name)
    if (file.isDefined) {
      file.get.isFile
    } else {
      false
    }
  }

  private final def find(name: String): Option[File] = {
    val files = walkTree(directory).filter(p => {
      p.getAbsolutePath.endsWith(name)
    })

    files.headOption
  }

  private final def walkTree(file: File): Iterable[File] = {
    val children = new Iterable[File] {
      def iterator: Iterator[File] = if (file.isDirectory && file.listFiles().length > 0) {
        file.listFiles.iterator
      } else Iterator.empty
    }

    Seq(file) ++: children.flatMap(walkTree)
  }

  private def cleanName(name: String): String = name.replace("/", "")
}
