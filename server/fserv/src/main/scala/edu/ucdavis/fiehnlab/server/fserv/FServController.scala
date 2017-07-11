package edu.ucdavis.fiehnlab.server.fserv


import java.io._
import javax.annotation.PostConstruct
import javax.servlet.annotation.MultipartConfig

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.{LocalLoader, ResourceLoader}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.core.io.InputStreamResource
import org.springframework.http.{HttpHeaders, MediaType, ResponseEntity}
import org.springframework.web.bind.annotation._
import org.springframework.web.multipart.MultipartFile

import scala.collection.JavaConverters._
import scala.collection.JavaConverters._

/**
  * provides an easy way to upload and download files from/to a central location.
  * Created by wohlgemuth on 7/7/17.
  */
@RestController
@RequestMapping(value = Array("/rest"))
class FServController extends LazyLogging {

  @Value("${wcms.server.fserv.directory:storage}")
  val directory: String = null

  @Autowired
  val resourceLoader: java.util.List[LocalLoader] = null

  @PostConstruct
  def init = {
    logger.info(s"defined ${resourceLoader.size()} loaders")
    val location = new File(directory)
    location.mkdirs()
    logger.info(s"storing data at location: ${location.getAbsolutePath}")
  }

  @RequestMapping(value = Array("/upload"), produces = Array(MediaType.APPLICATION_JSON_VALUE))
  def upload(@RequestParam("file") uploadedFileRef: MultipartFile): java.util.Map[String, _ <: Any] = {

    val fileName = uploadedFileRef.getOriginalFilename

    logger.info(s"uploaded file name is: ${fileName}")
    logger.info(s"content size is: ${uploadedFileRef.getSize}")

    // Now create the output file on the server.
    val outputFile = new File(generateFilePath(fileName))
    var reader: InputStream = null
    var writer: OutputStream = null
    var totalBytes = 0

    try {
      outputFile.createNewFile
      // Create the input stream to uploaded file to read data from it.
      reader = uploadedFileRef.getInputStream
      // Create writer for 'outputFile' to write data read from
      // 'uploadedFileRef'
      writer = new BufferedOutputStream(new FileOutputStream(outputFile, false))

      Iterator
        .continually(reader.read)
        .takeWhile(-1 !=)
        .foreach { x =>
          writer.write(x)
          totalBytes = totalBytes + 1
        }

      logger.info(s"wrote ${totalBytes} bytes")
      writer.flush()

      Map("message" -> "File successfully uploaded", "TotalBytesRead" -> totalBytes).asJava

    } catch {
      case e: Exception =>
        logger.error(e.getMessage, e)

        Map("message" -> "File upload failed", "error" -> e.getMessage).asJava

    } finally try {
      reader.close()
      writer.close()
    } catch {
      case e: IOException =>
        logger.error(e.getMessage, e)

        Map("message" -> "File upload failed", "error" -> e.getMessage).asJava
    }
  }

  @RequestMapping(path = Array("/download/{file:.+}"), method = Array(RequestMethod.GET))
  @throws[IOException]
  def download(@PathVariable("file") param: String): ResponseEntity[InputStreamResource] = {
    for (loader: ResourceLoader <- resourceLoader.asScala) {
      val file = loader.load(param)

      if (file.isDefined) {
        val headers = new HttpHeaders

        headers.add("Cache-Control", "no-cache, no-store, must-revalidate")
        headers.add("Pragma", "no-cache")
        headers.add("Expires", "0")

        val resource = new InputStreamResource(file.get)

        return ResponseEntity.ok.headers(headers).contentLength(file.get.available()).contentType(MediaType.parseMediaType("application/octet-stream")).body(resource)
      }
    }
    ResponseEntity.notFound().build()

  }

  @RequestMapping(path = Array("/exists/{file:.+}"), method = Array(RequestMethod.GET), produces = Array(MediaType.APPLICATION_JSON_VALUE))
  @throws[IOException]
  def exists(@PathVariable("file") param: String): ResponseEntity[java.util.Map[String, _ <: Any]] = {
    logger.info(s"checking if file exists: ${param}")
    for (loader: ResourceLoader <- resourceLoader.asScala) {
      logger.info(s"checking loader: ${loader}")
      if (loader.exists(param)) {
        logger.info("found file!")
        return ResponseEntity.ok.body(Map("exist" -> true, "file" -> param).asJava)
      }
    }
    logger.info("resource was not found")
    ResponseEntity.notFound().build()

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