package edu.ucdavis.fiehnlab.loader.impl

import java.io.{File, FileInputStream, InputStream}
import java.util.zip.ZipInputStream

import edu.ucdavis.fiehnlab.loader.LocalLoader
import org.springframework.stereotype.Component
import org.zeroturnaround.zip.ZipUtil

import scala.util.{Failure, Success, Try}

/**
  * loads the resource from the classpath
  */
@Component
class ClasspathResourceLoader extends LocalLoader {
  /**
    * returns the related resource or none
    *
    * @param name
    * @return
    */
  override def load(name: String): Option[InputStream] = {
    val fixed = cleanName(name)
    val resource = getClass.getResource(s"/${fixed}")

    logger.info(s"\tLoading resource: ${resource}")

    if (resource != null) {
      // this if can probably go away since we don't use .d anymore
      if (resource.getFile.endsWith(".d")) {
        val file = new File(resource.getFile)

        val zipfile = if (fixed.contains("/")) {
          File.createTempFile("tmp", s"${fixed.substring(fixed.lastIndexOf("/") + 1)}.zip")
        } else {
          File.createTempFile("tmp", s"${fixed}.zip")
        }

        ZipUtil.pack(file, zipfile)

        Option(new ZipInputStream(new FileInputStream(zipfile)))
      } else {
        Option(resource.openStream())
      }
    } else {
      None
    }
  }

  private def cleanName(name: String) = {
    if (name.startsWith("/")) { name.substring(1) } else { name }
  }

  override def exists(name: String): Boolean = {
    val file = if (name.startsWith("/")) {
      getClass.getResource(name)
    } else {
      getClass.getResource(s"/$name")
    }

    Try {
      new File(file.getFile)
    } match {
      case Success(f: File) => f.exists()
      case Failure(_) => false
    }
  }

  override def priority: Int = super.priority + 10

  override def isDirectory(name: String): Boolean = {
    val fixed = cleanName(name)
    val resource = getClass.getResource(s"/$fixed")
    if (resource != null) {
      new File(resource.getFile).isDirectory
    } else {
      false
    }
  }

  override def isFile(name: String): Boolean = {
    val fixed = cleanName(name)
    val resource = getClass.getResource(s"/$fixed")
    if (resource != null) {
      new File(resource.getFile).isFile
    } else {
      false
    }
  }
}
