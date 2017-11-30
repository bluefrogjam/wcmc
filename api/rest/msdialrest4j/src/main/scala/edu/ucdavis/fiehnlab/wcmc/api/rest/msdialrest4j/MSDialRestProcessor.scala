package edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j

import java.io._

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.core.io.FileSystemResource
import org.springframework.http.{HttpEntity, HttpHeaders, HttpMethod, MediaType, _}
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestOperations

@Configuration
@ComponentScan
class MSDialRestProcessorAutoconfiguration extends LazyLogging {
  @Bean
  def dfClient: DataFormerClient = new DataFormerClient()
}

/**
  * Created by wohlgemuth on 6/16/17.
  *
  * connects to the MSDial rest server
  */
@Component
class MSDialRestProcessor extends LazyLogging {

  @Value("${wcmc.api.rest.msdialrest4j.host:luna.fiehnlab.ucdavis.edu}")
  val msdresthost: String = ""

  @Value("${wcmc.api.rest.msdialrest4j.port:8080}")
  val msdrestport: Int = 0

  @Autowired
  val fServ4jClient: FServ4jClient = null

  @Autowired
  val resourceLoader: ResourceLoader = null

  @Autowired
  val restTemplate: RestOperations = null

  @Autowired
  val dfClient: DataFormerClient = null

  @Autowired
  val cvtSvc: ConvertService = null

  protected def msdresturl = s"http://${msdresthost}:${msdrestport}"

  /**
    * processes the input file and includes caching support
    * if enabled
    *
    * @param input
    * @return
    */
  def process(input: File): File = {
    logger.debug(s"processing file: ${input}")

    if (input.isDirectory) {
      throw new Exception("can't process a folder")
    } else {

      val converted: File = {
        if (!input.getName.endsWith(".abf")) {
          cvtSvc.getAbfFile(input).getOrElse(throw new Exception(s"Can't process ${input}. Error trying to convert to abf"))
        } else {
          input
        }
      }

      //upload file if not on msdial server
      if (!exists(converted.getName)) {
        logger.debug(s"${input.getName} doesn't exist, uploading...")
        upload(converted)
      }

      val url: String = s"${msdresturl}/rest/deconvolution/process/${converted.getName}"
      logger.debug(s"invoking: ${url}")
      val response = restTemplate.getForEntity(url, classOf[ServerResponse])

      logger.debug(s"response code is: ${response.getStatusCode} and message is ${response.getBody.message}")
      if (response.getStatusCode != HttpStatus.OK) {
        throw new Exception(s"Response was: ${response.getStatusCode} - ${response.getBody.message}")
      } else {
        download(converted.getName)
      }
    }
  }

  /**
    * uploads a given file to the server
    *
    * @param file
    * @return a tuple like (link to next step, auth token)
    */
  def upload(file: File): (String, String) = {
    logger.debug(s"uploading file: ${file} to ${msdresturl}")

    if(!file.exists()){
      throw new FileNotFoundException(s"provided file does not exist: ${file}")
    }
    val headers = new HttpHeaders
    headers.setContentType(MediaType.MULTIPART_FORM_DATA)

    val map = new LinkedMultiValueMap[String, AnyRef]
    map.add("file", new FileSystemResource(file))
    map.add("Name", file.getName)

    val requestEntity = new HttpEntity[LinkedMultiValueMap[String, AnyRef]](map, headers)

    val result = restTemplate.exchange(s"$msdresturl/rest/file/upload", HttpMethod.POST, requestEntity, classOf[ServerResponse])

    if (result.getStatusCode == HttpStatus.OK) {
      val token = result.getBody.filename
      logger.debug(s"filename = $token")

      if (result.getStatusCode == HttpStatus.OK) {
        //if input is raw data file... convert
        if (result.getBody.link.contains("/conversion/")) {
          (convert(result.getBody.link.split("/").last, token), token)
        } else { // process
          (result.getBody.link.split("/").last, token)
        }
      } else {
        throw new MSDialException(result.getBody)
      }
    } else {
      logger.warn(s"received result was: ${result}")
      throw new MSDialException(result.getBody)
    }

  }

  /**
    * was this process finished
    *
    * @param id
    * @return
    */
  protected def download(id: String): File = {
    val result = restTemplate.getForEntity(s"${msdresturl}/rest/deconvolution/status/${id}", classOf[ServerResponse])

    if (result.getStatusCode == HttpStatus.OK) {
      val download = restTemplate.getForEntity(s"${msdresturl}/rest/deconvolution/result/${id}", classOf[String])

      if (download.getStatusCode == HttpStatus.OK) {

        val file = File.createTempFile("msdial", "deco")

        val out = new BufferedWriter(new FileWriter(file))
        out.write(download.getBody)
        out.flush()
        out.close()

        file
      } else {
        throw new MSDialException(result.getBody)
      }
    } else {
      throw new MSDialException(result.getBody)
    }
  }

  /**
    * checks if a file exists on the server
    *
    * @param filename
    */
  protected def exists(filename: String): Boolean = {
    val url = s"${msdresturl}/rest/file/exists/${filename}"
    logger.debug(s"url: ${url}")

    val fresp = restTemplate.getForEntity(url, classOf[ServerResponse])
    val response = fresp.getBody

    response.exists
  }

  /**
    * convert the given file, if it was a .d file or so to an abf file
    *
    * @param id
    * @param token
    */
  protected def convert(id: String, token: String): String = {
    val url = s"${msdresturl}/rest/conversion/convert/${id}"
    logger.debug(s"invoking conversion at ${url}")
    val result = restTemplate.getForEntity(s"${msdresturl}/rest/conversion/convert/${id}", classOf[ServerResponse])

    if (result.getStatusCode == HttpStatus.OK) {
      logger.debug("conversion succeeded")
      result.getBody.link.split("/").last
    }
    else {
      throw new MSDialException(result.getBody)
    }

  }

  /**
    * schedules the processing of the actual object
    * for deconvolution
    *
    * @param id
    * @param token
    * @return
    */
  protected def schedule(id: String, token: String): String = {
    val result = restTemplate.getForEntity(s"${msdresturl}/rest/deconvolution/schedule/${id}", classOf[ServerResponse])

    if (result.getStatusCode == HttpStatus.OK) {
      logger.debug("conversion succeeded, not conversion required")
      result.getBody.link.split("/").last
    }
    else {
      throw new MSDialException(result.getBody)
    }
  }
}

/**
  * expected server response
  *
  * @param filename
  * @param link
  * @param message
  * @param error
  */
case class ServerResponse(filename: String, link: String, message: String, error: String, exists: Boolean)

/**
  * file check response
  *
  * @param filename
  * @param exists
  */
case class FileResponse(filename: String, exists: Boolean)

/**
  * an msdial exception if something goes wrong
  *
  * @param result
  */
class MSDialException(result: ServerResponse) extends Exception {
  override def getMessage: String = result.message + "\n" + result.error
}
