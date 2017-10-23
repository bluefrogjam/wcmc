package edu.ucdavis.fiehnlab.wcmc.api.rest.everything4j

import java.io.InputStream
import java.net.{URI, URL}

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.RemoteLoader
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.Configuration

/**
  * Created by wohlgemuth on 10/10/17.
  */
class Everything4J(host:String = "luna.fiehnlab.ucdavis.edu",port:Int = 80,enableLookup:Boolean = true) extends RemoteLoader with LazyLogging{

  /**
    * is a server allowed to use this one for lookup
    * functionality
    */
  override def isLookupEnabled(): Boolean = enableLookup

  @Autowired
  val objectMapper:ObjectMapper = null

  /**
    * returns the related resource or none
    *
    * @param name
    * @return
    */
  override def load(name: String): Option[InputStream] = {
    val url = s"http://${host}:${port}?s=${name}&j=1&path_column=1"
    logger.info(s"checking url: ${url}")

    val data = objectMapper.readValue(new URL(url),classOf[Search]).results.filter(_.`type`.toLowerCase() == "file")

    if(data.isEmpty){
      None
    }
    else{
      val encoded = data.head.path.replaceAll("\\\\","/").replaceAll("\\s","%20").replaceAll(":","%3A")
      val content = new URI(s"http://${host}:${port}/${encoded}").toURL

      Option(content.openStream())

    }
  }

  /**
    * does the given resource exists
    *
    * @param name
    * @return
    */
  override def exists(name: String): Boolean = {
    val url = s"http://${host}:${port}?s=${name}&j=1&path_column=1"
    logger.info(s"checking url: ${url}")

    objectMapper.readValue(new URL(url), classOf[Search]).results.exists(_.`type`.toLowerCase() == "file")

  }
}

@Configuration
//@Import(Array(classOf[CaseClassToJSONSerializationConfiguration]))
class Everything4JConfiguration

case class Results(
                    `type`: String,
                    name: String,
                    path:String
                  )
case class Search(
                   totalResults: Double,
                   results: List[Results]
                 )
