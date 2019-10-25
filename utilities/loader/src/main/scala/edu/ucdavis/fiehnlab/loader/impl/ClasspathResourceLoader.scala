package edu.ucdavis.fiehnlab.loader.impl

import java.io.{File, InputStream}

import edu.ucdavis.fiehnlab.loader.LocalLoader
import org.springframework.stereotype.Component

import scala.util.{Failure, Success, Try}

/**
  * loads the resource from the classpath
  */
@Component
class ClasspathResourceLoader extends LocalLoader {
  logger.info(s"Creating ClasspathResourceLoader with prioroty: ${priority}")
  /**
    * returns the related resource or none
    *
    * @param name
    * @return
    */
  override def load(name: String): Option[InputStream] = {
    val fixed = cleanName(name)
    val resource = getClass.getResourceAsStream(fixed)

    logger.debug(s"\tLoading resource: ${resource} (fixed to: $fixed)")
    Option(resource)
  }

  private def cleanName(name: String): String = {
    if (name.startsWith("/")) {
      name
    } else {
      logger.debug(s"fixed name $name to: /$name")
      "/".concat(name)
    }
  }

  override def exists(name: String): Boolean = {
    val fixed = cleanName(name)
    val resource = getClass.getResource(fixed)

    Try {
      new File(resource.getFile)
    } match {
      case Success(f: File) => f.exists()
      case Failure(_) => false
    }
  }

  override def priority: Int = super.priority + 20

  override def isDirectory(name: String): Boolean = {
    val fixed = cleanName(name)
    val resource = getClass.getResource(fixed)
    if (resource != null) {
      new File(resource.getFile).isDirectory
    } else {
      false
    }
  }

  override def isFile(name: String): Boolean = {
    val fixed = cleanName(name)
    val resource = getClass.getResource(fixed)
    if (resource != null) {
      new File(resource.getFile).isFile
    } else {
      false
    }
  }

  override def toString: String = {
    s"${this.getClass.getSimpleName} (${this.priority})"
  }
}
