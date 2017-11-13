package edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j

import java.io.{ByteArrayInputStream, File, FileNotFoundException, InputStream}
import java.util

import edu.ucdavis.fiehnlab.loader.RemoteLoader
import org.springframework.core.io.FileSystemResource
import org.springframework.http._
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client._

/**
  * Provides a simple access service to the remote FServ Server. Which provides shared resources over
  * a local or remote network
  * Created by wohlgemuth on 7/9/17.
  */
class FServ4jClient(host:String = "127.0.0.1",port:Int = 80, enableLookup:Boolean = false,root:String="rest/file") extends RemoteLoader {

  /**
    * is a server allowed to use this one for lookup
    * functionality
    */
  override def isLookupEnabled(): Boolean = enableLookup

  val template: RestOperations = new RestTemplate()

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
    logger.info(s"looking for file: ${location}")
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
    * priority of the loader
    *
    * @return
    */
  override def priority = -1000

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

      logger.info(s"uploading too: ${url}")
      val result = template.exchange(s"$url/upload", HttpMethod.POST, requestEntity, classOf[Any])

      if (result.getStatusCode != HttpStatus.OK) {
        throw new HttpServerErrorException(result.getStatusCode)
      }

    } else {
      throw new FileNotFoundException(s"file was not found locally: ${file.getAbsolutePath}")
    }
  }
}
