package edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j

import java.io.{ByteArrayInputStream, File, FileNotFoundException, InputStream}
import java.util

import edu.ucdavis.fiehnlab.loader.{RemoteLoader, ResourceLoader}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.io.FileSystemResource
import org.springframework.http._
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client._

/**
  * Provides a simple access service to the remote FServ Server. Which provides shared resources over
  * a local or remote network
  * Created by wohlgemuth on 7/9/17.
  */
@Component
class FServ4jClient extends RemoteLoader {

  @Autowired
  val template: RestOperations = null


  @Value("${wcmc.api.rest.fserv4j.host:127.0.0.1}")
  val host: String = ""

  @Value("${wcmc.api.rest.fserv4j.port:8080}")
  val port: Int = 80

  @Value("${wcmc.api.rest.fserv4j.root:rest}")
  val root: String = ""

  /**
    * generates the url for us to access the server
    *
    * @return
    */
  def url = s"http://$host:$port/$root"

  /**
    * returns the related resource or none
    *
    * @param name
    * @return
    */
  override def load(name: String): Option[InputStream] = {
    if (exists(name)) {
      val headers = new HttpHeaders
      headers.setAccept(util.Arrays.asList(MediaType.APPLICATION_OCTET_STREAM))

      val entity = new HttpEntity[String](headers)

      val myUrl = s"$url/download/$name"
      logger.debug(s"download resource from: ${myUrl}")
      val response = template.exchange(myUrl, HttpMethod.GET, entity, classOf[Array[Byte]])

      logger.debug(s"size:${response.getBody.size}")
      if (response.getStatusCode == HttpStatus.OK) {
        Option(new ByteArrayInputStream(response.getBody))
      }
      else {
        throw new HttpServerErrorException(response.getStatusCode)
      }
    }
    else {
      None
    }

  }

  /**
    * simple wrapper to make the api more easy to understand
    *
    * @param name
    * @return
    */
  final def download(name: String): Option[InputStream] = load(name)

  /**
    * does the given resource exists
    *
    * @param name
    * @return
    */
  override def exists(name: String): Boolean = {
    val location = s"$url/exists/$name"
    logger.debug(s"looking for location: ${location}")
    try {
      val response = template.getForEntity(location, classOf[Any])
      response.getStatusCode == HttpStatus.OK
    }
    catch {
      case x: HttpClientErrorException =>
        if (x.getStatusCode == HttpStatus.NOT_FOUND) {
          false
        }
        else {
          throw x
        }

      //in case the server is offline
      case x: ResourceAccessException =>
        logger.debug(s"this can be ignored: ${x.getMessage}", x)
        false
    }
  }

  /**
    * uploads the given file to the server
    *
    * @param file
    * @return
    */
  def upload(file: File,name:Option[String] = None) = {
    if (file.exists()) {
      val map = new LinkedMultiValueMap[String, AnyRef]
      map.add("file", new FileSystemResource(file))
      map.add("name", if(name.isDefined){name.get} else{file.getName})

      val headers = new HttpHeaders
      headers.setContentType(MediaType.MULTIPART_FORM_DATA)

      val requestEntity = new HttpEntity[LinkedMultiValueMap[String, AnyRef]](map, headers)
      val result = template.exchange(s"$url/upload", HttpMethod.POST, requestEntity, classOf[Any])

      if (result.getStatusCode != HttpStatus.OK) {
        throw new HttpServerErrorException(result.getStatusCode)
      }

    } else {
      throw new FileNotFoundException(file.getAbsolutePath)
    }
  }
}
