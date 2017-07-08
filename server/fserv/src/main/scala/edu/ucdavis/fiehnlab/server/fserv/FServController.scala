package edu.ucdavis.fiehnlab.server.fserv


import java.io._
import javax.annotation.PostConstruct
import javax.servlet.annotation.MultipartConfig

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.ResourceLoader
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
@RestController
@RequestMapping(value = Array("/rest")) //Max uploaded file size (here it is ~2GB)
@MultipartConfig(fileSizeThreshold = 2097152000)
class FServController extends LazyLogging {

  @Value("${wcms.server.fserv.directory:storage}")
  val directory: String = null

  @Autowired
  val resourceLoader: java.util.List[ResourceLoader] = null

  @PostConstruct
  def init = {
    logger.info("configurating...")
    val location = new File(directory)
    location.mkdirs()
    logger.info(s"storing data at location: ${location}")
  }

  @RequestMapping(value = Array("/upload"))
  def upload(@RequestParam("file") uploadedFileRef: MultipartFile): String = {

    val fileName = uploadedFileRef.getOriginalFilename

    // Path where the uploaded file will be stored.
    // This buffer will store the data read from 'uploadedFileRef'
    val buffer = new Array[Byte](1000)
    // Now create the output file on the server.
    val outputFile = new File(generateFilePath(fileName))
    var reader: InputStream = null
    var writer: OutputStream = null
    var totalBytes = 0

    try {
      outputFile.createNewFile
      // Create the input stream to uploaded file to read data from it.
      reader = uploadedFileRef.getInputStream.asInstanceOf[FileInputStream]
      // Create writer for 'outputFile' to write data read from
      // 'uploadedFileRef'
      writer = new BufferedOutputStream(new FileOutputStream(outputFile,false))

      Iterator
        .continually (reader.read)
        .takeWhile (-1 !=)
        .foreach { x =>
          writer.write(x)
          totalBytes = totalBytes + 1
        }

      writer.flush()

      s"""{ "message" : "File uploaded successfully", "TotalBytesRead" : ${totalBytes}}"""

    } catch {
      case e: IOException =>
        logger.error(e.getMessage, e)

        s"""{ "message" : "File upload failed", "error" : ${e.getMessage}}"""

    } finally try {
      reader.close()
      writer.close()
    } catch {
      case e: IOException =>
        logger.error(e.getMessage, e)

        s"""{ "message" : "File upload failed", "error" : ${e.getMessage}}"""
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

  /**
    * generates the path where the given file should be stored
    *
    * @param fileName
    * @return
    */
  private def generateFilePath(fileName: String) = {
    new File(s"${directory}/${fileName}").getAbsolutePath
  }
}