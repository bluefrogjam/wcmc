package edu.ucdavis.fiehnlab.loader.impl

import java.io.InputStream

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import org.springframework.stereotype.Component

/**
  * loads the resource from the classpath
  */
@Component
class ClasspathResourceLoader extends ResourceLoader {
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
}
