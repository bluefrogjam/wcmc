package edu.ucdavis.fiehnlab.loader.impl

import java.io.{File, InputStream}

import edu.ucdavis.fiehnlab.loader.{LocalLoader, ResourceLoader}
import org.springframework.stereotype.Component

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
  override def load(name: String): Option[InputStream] = if (name.startsWith("/")) {
    Option(getClass.getResourceAsStream(name))
  } else {
    Option(getClass.getResourceAsStream(s"/$name"))
  }

  override def exists(name: String): Boolean = {
    val file = if (name.startsWith("/")) {
      getClass.getResource(name)
    } else {
      getClass.getResource(s"/${name}")
    }

    Try {
      new File(file.getFile)
    } match {
      case Success(f: File) => f.exists()
      case Failure(_) => false
    }
  }
}
