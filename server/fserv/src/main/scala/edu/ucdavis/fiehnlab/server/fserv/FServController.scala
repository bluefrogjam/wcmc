package edu.ucdavis.fiehnlab.server.fserv


import java.io.{File, FileInputStream, FileOutputStream, IOException}
import javax.servlet.annotation.MultipartConfig

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.ResourceLoader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.{HttpHeaders, MediaType, ResponseEntity}
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod, RequestParam, RestController}
import org.springframework.web.multipart.MultipartFile
import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 7/7/17.
  */


@RestController
@RequestMapping(value = Array("/rest")) //Max uploaded file size (here it is ~2GB)
@MultipartConfig(fileSizeThreshold = 2097152000)
class FServController extends LazyLogging {

  @Autowired
  val resourceLoader: java.util.List[ResourceLoader] = null

  @RequestMapping(value = Array("/upload"))
  def upload(@RequestParam("uploadedFile") uploadedFileRef: MultipartFile): String = {

    val fileName = uploadedFileRef.getOriginalFilename

    // Path where the uploaded file will be stored.
    val path = generateFilePath(fileName)
    // This buffer will store the data read from 'uploadedFileRef'
    val buffer = new Array[Byte](1000)
    // Now create the output file on the server.
    val outputFile = new File(path)
    var reader: FileInputStream = null
    var writer: FileOutputStream = null
    var totalBytes = 0

    try {
      outputFile.createNewFile
      // Create the input stream to uploaded file to read data from it.
      reader = uploadedFileRef.getInputStream.asInstanceOf[FileInputStream]
      // Create writer for 'outputFile' to write data read from
      // 'uploadedFileRef'
      writer = new FileOutputStream(outputFile)
      // Iteratively read data from 'uploadedFileRef' and write to
      // 'outputFile';
      var bytesRead: Int = 0
      while ( {
        (bytesRead = reader.read(buffer)) != -1
      }) {
        writer.write(buffer)
        totalBytes += bytesRead
      }

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

  @RequestMapping(path = Array("/download"), method = Array(RequestMethod.GET))
  @throws[IOException]
  def download(param: String): ResponseEntity[InputStreamResource] = {
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
    new File("storage/" + fileName).getAbsolutePath
  }
}