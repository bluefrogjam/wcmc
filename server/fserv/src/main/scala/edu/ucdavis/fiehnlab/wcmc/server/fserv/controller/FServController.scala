package edu.ucdavis.fiehnlab.wcmc.server.fserv.controller

import java.io._
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletResponse

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.{LocalLoader, RemoteLoader, ResourceLoader}
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.core.io.InputStreamResource
import org.springframework.http.{HttpHeaders, MediaType, ResponseEntity}
import org.springframework.web.bind.annotation._
import org.springframework.web.multipart.MultipartFile

import scala.collection.JavaConverters._

/**
  * provides an easy way to upload and download files from/to a central location.
  * Created by wohlgemuth on 7/7/17.
  */
@CrossOrigin
@RestController
@RequestMapping(value = Array("/rest/file"))
class FServController extends LazyLogging {

  @Value("${wcmc.server.fserv.directory:storage}")
  val directory: String = null

  @Autowired
  val resourceLoader: java.util.List[ResourceLoader] = null

  @PostConstruct
  def init = {
    logger.info(s"defined ${resourceLoader.size()} loaders")

    resourceLoader.asScala.foreach { loader =>
      logger.info(s"defined loader: ${loader}")
    }

    val location = new File(directory)
    location.mkdirs()
    logger.info(s"storing data at location: ${location.getAbsolutePath}")

  }

  @RequestMapping(value = Array("/upload"), produces = Array(MediaType.APPLICATION_JSON_VALUE))
  def upload(@RequestParam("file") uploadedFileRef: MultipartFile, @RequestParam(name = "name", required = false, defaultValue = "") name: String): java.util.Map[String, _ <: Any] = {


    val fileName = if (name == "") uploadedFileRef.getOriginalFilename else name

    logger.info(s"uploaded file name is: ${name}")
    logger.info(s"content size is: ${uploadedFileRef.getSize}")

    // Now create the output file on the server.
    val outputFile = new File(generateFilePath(fileName))
    var reader: InputStream = null
    var writer: FileOutputStream = null
    var totalBytes = 0L

    try {
      outputFile.createNewFile
      // Create the input stream to uploaded file to read data from it.
      reader = uploadedFileRef.getInputStream
      // Create writer for 'outputFile' to write data read from
      // 'uploadedFileRef'
      writer = new FileOutputStream(outputFile, false)

      IOUtils.copy(reader, writer)
      writer.flush()

      totalBytes = outputFile.length
      logger.info(s"wrote ${totalBytes} bytes")

      Map("message" -> "File successfully uploaded", "TotalBytesRead" -> totalBytes).asJava

    } catch {
      case e: Exception =>
        logger.error(e.getMessage, e)

        Map("message" -> "File upload failed", "error" -> e.getMessage).asJava

    } finally {
      try {
        reader.close()
      } catch {
        case e: IOException =>
          logger.error(e.getMessage, e)
          Map("message" -> "File upload failed", "error" -> e.getMessage).asJava
      }

      // after flush writer should be empty, next line avoids reporting 'fake?' IOException, on successful upload.
      IOUtils.closeQuietly(writer)
    }
  }

  @RequestMapping(path = Array("/download/{file:.+}"), method = Array(RequestMethod.GET), produces = Array(MediaType.APPLICATION_OCTET_STREAM_VALUE))
  @throws[IOException]
  def download(@PathVariable("file") param: String): ResponseEntity[InputStreamResource] = {


    val response = resourceLoader.asScala.sortBy(_.priority).reverse.collectFirst {

      //loader is disabled
      case x: RemoteLoader if x.isLookupEnabled() && x.load(param).isDefined =>
        val headers = new HttpHeaders

        headers.add("Cache-Control", "no-cache, no-store, must-revalidate")
        headers.add("Pragma", "no-cache")
        headers.add("Expires", "0")

        val resource = new InputStreamResource(x.load(param).get)

        ResponseEntity.ok.headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource)

      //process as normal
      case x: LocalLoader if x.load(param).isDefined =>
        val headers = new HttpHeaders

        headers.add("Cache-Control", "no-cache, no-store, must-revalidate")
        headers.add("Pragma", "no-cache")
        headers.add("Expires", "0")

        val resource = new InputStreamResource(x.load(param).get)

        ResponseEntity.ok.headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource)
    }

    if (response.isDefined) {
      response.get
    }
    else {
      ResponseEntity.notFound().build()
    }

  }

  @RequestMapping(path = Array("/exists/{file:.+}"), method = Array(RequestMethod.GET))
  @throws[IOException]
  def exists(@PathVariable("file") param: String, responseBody: HttpServletResponse): java.util.Map[String, _ <: Any] = {
    logger.info(s"checking if file exists: ${param}")
    val exists = resourceLoader.asScala.sortBy(_.priority).reverse.collectFirst {
      case loader: RemoteLoader if loader.isLookupEnabled() && loader.exists(param) =>
        Map("exist" -> true, "file" -> param).asJava
      case loader: LocalLoader if loader.exists(param) =>
        Map("exist" -> true, "file" -> param).asJava
    }

    if (exists.isDefined) {
      exists.get
    }
    else {
      logger.info(s"resource was not found: ${param}")
      responseBody.setStatus(HttpServletResponse.SC_NOT_FOUND)
      Map("exist" -> false, "file" -> param).asJava
    }
  }


  /**
    * generates the path where the given file should be stored
    *
    * @param fileName
    * @return
    */
  private def generateFilePath(fileName: String) = {
    val fileDir = new File(directory)

    if (!fileDir.exists()) {
      fileDir.mkdirs()
    }

    val location = new File(s"${directory}/${fileName}").getAbsolutePath

    logger.info(s"storing data at: ${location}")
    location
  }
}
