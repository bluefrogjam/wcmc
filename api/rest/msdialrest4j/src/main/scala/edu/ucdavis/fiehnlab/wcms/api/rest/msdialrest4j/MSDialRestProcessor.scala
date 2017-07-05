package edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j

import java.io._

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcms.utilities.ZipUtil
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.core.io.FileSystemResource
import org.springframework.http._
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations

/**
  * Created by wohlgemuth on 6/16/17.
  *
  * connects to the MSDial rest server
  */
@Component
class MSDialRestProcessor extends LazyLogging {

  @Value("${wcms.api.rest.msdialrest4j.host}")
  val host: String = "128.120.143.101"

  @Value("${wcms.api.rest.msdialrest4j.port}")
  val port: Int = 80

  @Autowired
  val restTemplate: RestOperations = null

	protected def url = s"http://${host}:${port}"

  /**
    * processes the input file and
    *
    * @param input
    * @return
    */
  def process(input: File): File = {

    //if directory and ends with .d zip file
    val (toUpload, temp) = if (input.isDirectory && input.getName.endsWith(".d")) {

      val temp = File.createTempFile("wcms", ".zip")
      ZipUtil.zipDir(input.getAbsolutePath, temp.getAbsolutePath, s"${input.getName}")
      (temp, true)
    }
    else {
      (input, false)
    }

    try {
      //upload
      val (uploadId, token) = upload(toUpload)
      logger.debug(s"Got sample ID $uploadId for $toUpload")

      //schedule
      val scheduleId = schedule(uploadId, token)

      //download processed id
      download(scheduleId, token)
    }
    finally {
      if (temp) {
        logger.debug(s"removing temp file: ${temp}")
        toUpload.delete()
      }
    }
  }

  /**
    * uploads a given file to the server
    *
    * @param file
    * @return
    */
  protected def upload(file: File): (String, String) = {

    import org.springframework.http.{HttpEntity, HttpHeaders, HttpMethod, MediaType}
    import org.springframework.util.LinkedMultiValueMap

    val map = new LinkedMultiValueMap[String, AnyRef]
    map.add("file", new FileSystemResource(file))
    val headers = new HttpHeaders
    headers.setContentType(MediaType.MULTIPART_FORM_DATA)

    val requestEntity = new HttpEntity[LinkedMultiValueMap[String, AnyRef]](map, headers)
    val result = restTemplate.exchange(s"$url/rest/upload", HttpMethod.POST, requestEntity, classOf[ServerResponse])

	  val token = result.getBody.filename
	  logger.info(s"filename = $token")

    if (result.getStatusCode == HttpStatus.OK) {
      if (result.getBody.link.contains("/conversion/")) {
        logger.debug(s"upload result requires conversion of data")
	      (convert(result.getBody.link.split("/").last, token), token)
      } else {
        logger.debug("upload succeeded, not conversion required")
	      (result.getBody.link.split("/").last, token)
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
	  * @param token
    * @return
    */
  protected def download(id: String, token: String): File = {
    val result = restTemplate.exchange(s"$url/rest/deconvolution/status/${id}", HttpMethod.GET, createAuthRequest(token), classOf[ServerResponse])

    if (result.getStatusCode == HttpStatus.OK) {
      val resultId = result.getBody.link.split("/").last

      val download = restTemplate.exchange(s"$url/rest/deconvolution/result/${id}",HttpMethod.GET, createAuthRequest(token), classOf[String])

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
	  * @param token
    */
  protected def convert(id: String, token: String): String = {
    val result = restTemplate.exchange(s"$url/rest/conversion/convert/${id}", HttpMethod.GET, createAuthRequest(token), classOf[ServerResponse])

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
    val result = restTemplate.exchange(s"$url/rest/deconvolution/schedule/${id}", HttpMethod.GET, createAuthRequest(token), classOf[ServerResponse])

    if (result.getStatusCode == HttpStatus.OK) {
      logger.debug("conversion succeeded, not conversion required")
      result.getBody.link.split("/").last
    }
    else {
      throw new MSDialException(result)
    }
  }

	/**
		* Creates an HttpEntity with an Authorization header to call the rest server
		* @param token authorization token returned by the upload endpoint
		* @return
		*/
	protected def createAuthRequest(token: String): HttpEntity[String] = {
		// creating authorized request
		val headers: HttpHeaders = new HttpHeaders()
		headers.add("Authorization", s"Bearer $token")

		val request: HttpEntity[String] = new HttpEntity("parameters", headers)
		request
	}

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
