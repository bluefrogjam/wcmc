package edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j

import java.io._
import java.util.zip.ZipOutputStream

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcms.utilities.ZipUtil
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.core.io.FileSystemResource
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations

/**
  * Created by wohlgemuth on 6/16/17.
  *
  * connects to the MSDial rest server
  */
@Component
class MSDialRestProcessor extends LazyLogging {

  @Value("${wcms.api.rest.msdialrest4j.host:128.120.143.101}")
  val host: String = ""

  @Value("${wcms.api.rest.msdialrest4j.port:80}")
  val port: Int = 80

  @Autowired
  val restTemplate: RestOperations = null

  /**
    * processes the input file and
    *
    * @param input
    * @return
    */
  def process(input: File): File = {

    //if directory and ends with .d zip file
    val toUpload = if (input.isDirectory && input.getName.endsWith(".d")) {

      val temp = File.createTempFile("wcms", ".zip")
      logger.debug(s"compressing to local file: ${temp} prior to upload")
      temp.deleteOnExit()
      val out = new ZipOutputStream(new FileOutputStream(temp))
      ZipUtil.zip(input, out)
      out.flush()
      out.close()
      temp
    }
    else {
      input
    }

    //upload
    val uploadId = upload(toUpload)

    //schedule
    val scheduleId = schedule(uploadId)

    //download processed id
    download(scheduleId)
  }

  /**
    * uploads a given file to the server
    *
    * @param file
    * @return
    */
  protected def upload(file: File): String = {

    import org.springframework.http.{HttpEntity, HttpHeaders, HttpMethod, MediaType}
    import org.springframework.util.LinkedMultiValueMap

    val map = new LinkedMultiValueMap[String, AnyRef]
    map.add("file", new FileSystemResource(file))
    val headers = new HttpHeaders
    headers.setContentType(MediaType.MULTIPART_FORM_DATA)

    val requestEntity = new HttpEntity[LinkedMultiValueMap[String, AnyRef]](map, headers)
    val result = restTemplate.exchange(s"$url/rest/upload", HttpMethod.POST, requestEntity, classOf[ServerResponse])

    if (result.getStatusCode == HttpStatus.OK) {
      if (result.getBody.link.contains("/conversion/")) {
        logger.debug("upload result requires conversion of data")
        convert(result.getBody.link.split("/").last)
      }
      else {
        logger.debug("upload succeeded, not conversion required")
        result.getBody.link.split("/").last
      }
    }
    else {
      throw new MSDialException(result)
    }
  }

  /**
    * was this process finished
    *
    * @param id
    * @return
    */
  protected def download(id: String): File = {
    val result = restTemplate.getForEntity(s"$url/rest/deconvolution/status/${id}", classOf[ServerResponse])

    if (result.getStatusCode == HttpStatus.OK) {
      val resultId = result.getBody.link.split("/").last

      val download = restTemplate.getForEntity(s"$url/rest/deconvolution/result/${id}", classOf[String])

      if (download.getStatusCode == HttpStatus.OK) {

        val file = File.createTempFile("msdial", "deco")

        val out = new BufferedWriter(new FileWriter(file))
        out.write(download.getBody)
        out.flush()
        out.close()

        file
      }
      else {
        throw new MSDialException(result)
      }
    }
    else {
      throw new MSDialException(result)
    }
  }

  /**
    * convert the given file, if it was a .d file or so to an abf file
    *
    * @param id
    */
  protected def convert(id: String): String = {
    val result = restTemplate.getForEntity(s"$url/rest/conversion/convert/${id}", classOf[ServerResponse])

    if (result.getStatusCode == HttpStatus.OK) {
      logger.debug("conversion succeeded, not conversion required")
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
    * @return
    */
  protected def schedule(id: String): String = {
    val result = restTemplate.getForEntity(s"$url/rest/deconvolution/schedule/${id}", classOf[ServerResponse])

    if (result.getStatusCode == HttpStatus.OK) {
      logger.debug("conversion succeeded, not conversion required")
      result.getBody.link.split("/").last
    }
    else {
      throw new MSDialException(result)
    }
  }

  protected def url = s"http://${host}:${port}"

  /**
    * defined server response
    *
    * @param filename
    * @param link
    * @param message
    * @param error
    */

}

/**
  * exspected server response
  *
  * @param filename
  * @param link
  * @param message
  * @param error
  */
case class ServerResponse(filename: String, link: String, message: String, error: String)

/**
  * an msdial exception if something goes wrong
  *
  * @param result
  */
class MSDialException(result: ResponseEntity[ServerResponse]) extends Exception