package edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j

import java.io._

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.core.io.FileSystemResource
import org.springframework.http.{HttpEntity, HttpHeaders, HttpMethod, MediaType, ResponseEntity, _}
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.{HttpServerErrorException, RestTemplate}

@Configuration
class MSDialRestProcessorAutoconfiguration extends LazyLogging {
  @Bean
  def restTemplate(): RestTemplate = new RestTemplate()

  @Bean
  def fServ4jClient = new FServ4jClient()

}

/**
  * Created by wohlgemuth on 6/16/17.
  *
  * connects to the MSDial rest server
  */
@Component
class MSDialRestProcessor extends LazyLogging {

  @Value("${wcmc.api.rest.msdialrest4j.host:phobos.fiehnlab.ucdavis.edu}")
  val msdresthost: String = ""

  @Value("${wcmc.api.rest.msdialrest4j.port:80}")
  val msdrestport: Int = 0

  @Autowired
  val fServ4jClient: FServ4jClient = null

  @Autowired
  val resourceLoader: ResourceLoader = null

  @Autowired
  val restTemplate: RestTemplate = null

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

    var msdialFile: File = new File(input.getName.substring(0, input.getName.indexOf(".")).concat(".msdial"))

    if (input.isDirectory) {
      throw new Exception("can't process a folder")
    } else {
      //upload file if not on msdial server
      if (!exists(input.getName)) {
        logger.debug(s"${input.getName} doesn't exist, uploading...")
        upload(input)
      } else {
        logger.debug(s"${input.getName} exists")
      }

      val response = restTemplate.getForEntity(s"${msdresturl}/rest/deconvolution/process/${input.getName}", classOf[String])
      logger.debug(s"RESPONSE: ${response}")

      if (response.getStatusCode != HttpStatus.OK) {
        throw new Exception("Bad request")
      } else {
        val fout = new FileOutputStream(msdialFile)

        try {
          IOUtils.copyLarge(fServ4jClient.download(msdialFile.getName).get, fout)
          msdialFile
        } finally {
          fout.flush()
          fout.close()
        }
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

    val headers = new HttpHeaders
    headers.setContentType(MediaType.MULTIPART_FORM_DATA)

    val map = new LinkedMultiValueMap[String, File]
    map.add("file", file)

    val requestEntity = new HttpEntity[LinkedMultiValueMap[String, File]](map, headers)

    try {
      val result = restTemplate.postForEntity(s"$msdresturl/rest/upload", requestEntity, classOf[ServerResponse])

      if (result.getStatusCode == HttpStatus.OK) {
        val token = result.getBody.filename
        logger.info(s"filename = $token")

        if (result.getStatusCode == HttpStatus.OK) {
          //if input is raw data file... convert
          if (result.getBody.link.contains("/conversion/")) {
            logger.debug(s"upload result requires conversion of data")
            (convert(result.getBody.link.split("/").last, token), token)
          } else { // process
            logger.debug("upload succeeded, not conversion required")
            (result.getBody.link.split("/").last, token)
          }
        } else {
          throw new MSDialException(result)
        }
      } else {
        logger.warn(s"received result was: ${result}")
        throw new MSDialException(result)
      }
    } catch {
      case ex: HttpServerErrorException => logger.error(ex.getMessage)
      ("", "")
    }
  }

  /**
    * was this process finished
    *
    * @param id
    * @param token
    * @return
    */
  protected def download(id: String, token: String): File = {
    val result = restTemplate.getForEntity(s"${msdresturl}/rest/deconvolution/status/${id}", classOf[ServerResponse])

    if (result.getStatusCode == HttpStatus.OK) {
      //      val resultId = result.getBody.link.split("/").last

      val download = restTemplate.getForEntity(s"${msdresturl}/rest/deconvolution/result/${id}", classOf[String])

      if (download.getStatusCode == HttpStatus.OK) {

        val file = File.createTempFile("msdial", "deco")

        val out = new BufferedWriter(new FileWriter(file))
        out.write(download.getBody)
        out.flush()
        out.close()

        file
      } else {
        throw new MSDialException(result)
      }
    } else {
      throw new MSDialException(result)
    }
  }

  /**
    * checks if a file exists on the server
    *
    * @param filename
    */
  protected def exists(filename: String): Boolean = {
    val fresp = restTemplate.getForEntity(s"${msdresturl}/rest/file/exists/${filename}", classOf[String])
    logger.info(s"EXISTS: ${fresp.getBody} ")

    //TODO: FIX this horrible hack since restTemplate can't deal with booleans in json responses
    fresp.getBody.contains("exists\":true")
  }

  /**
    * convert the given file, if it was a .d file or so to an abf file
    *
    * @param id
    * @param token
    */
  protected def convert(id: String, token: String): String = {
    val result = restTemplate.getForEntity(s"${msdresturl}/rest/conversion/convert/${id}", classOf[ServerResponse])

    if (result.getStatusCode == HttpStatus.OK) {
      logger.debug("conversion succeeded")
      result.getBody.link.split("/").last
    }
    else {
      throw new MSDialException(result)
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
      throw new MSDialException(result)
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
case class ServerResponse(filename: String, link: String, message: String, error: String)

/**
  * file check response
  *
  * @param filename
  * @param exists
  */
@JsonCreator
case class FileResponse(@JsonProperty("filename")filename: String, @JsonProperty("exists")exists: Boolean)

/**
  * an msdial exception if something goes wrong
  *
  * @param result
  */
class MSDialException(result: ResponseEntity[ServerResponse]) extends Exception {
  override def getMessage: String = result.getBody.message + "\n" + result.getBody.error
}

/**
  * utilizes our FServer to cache processing results somewhere on the file system o
  */
class CachedMSDialRestProcesser extends MSDialRestProcessor {
  /**
    * processes the input file and includes caching support
    * if enabled
    *
    * @param input
    * @return
    */
  override def process(input: File): File = {

    val newFile = s"${input.getName}"

    if (!resourceLoader.exists(newFile)) {
      logger.info(s"file: ${input.getName} requires processing and will be stored as ${newFile}")
      fServ4jClient.upload(super.process(input), name = Some(newFile))
    }

    resourceLoader.loadAsFile(newFile).get
  }
}
