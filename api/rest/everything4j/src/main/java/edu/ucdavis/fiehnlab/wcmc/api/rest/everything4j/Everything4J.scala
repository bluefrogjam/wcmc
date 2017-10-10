package edu.ucdavis.fiehnlab.wcmc.api.rest.everything4j

import java.io.InputStream
import java.net.{URI, URL, URLEncoder}
import java.nio.charset.StandardCharsets

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.RemoteLoader
import edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config.CaseClassToJSONSerializationConfiguration
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.{Configuration, Import, Profile}
import org.springframework.stereotype.Component
import org.springframework.web.util.UriUtils

/**
  * Created by wohlgemuth on 10/10/17.
  */
@Component
@Profile(Array("wcmc.api.rest.everything4j"))
class Everything4J extends RemoteLoader with LazyLogging{

  @Value("${wcmc.api.rest.everything4j.host:luna.fiehnlab.ucdavis.edu}")
  val host: String = ""

  @Value("${wcmc.api.rest.everything4j.port:80}")
  val port: Int = 80

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
@Import(Array(classOf[CaseClassToJSONSerializationConfiguration]))
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
