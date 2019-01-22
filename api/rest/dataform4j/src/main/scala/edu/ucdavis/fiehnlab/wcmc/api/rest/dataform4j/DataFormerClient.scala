package edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j

import java.io._
import java.net.URL

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.loader.{DelegatingResourceLoader, ResourceLoader}
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.FileType.FileType
import org.apache.commons.io.IOUtils
import org.apache.http.impl.client.HttpClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.core.io.ByteArrayResource
import org.springframework.http._
import org.springframework.http.client.{ClientHttpRequestFactory, HttpComponentsClientHttpRequestFactory}
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

/**
  * Created by diego on 8/30/2017.
  *
  * sends a raw data file to DataFormer rest service to be converted into .abf and .mzml and then sends the result to fserv
  */
class DataFormerClient(fserv4j:ResourceLoader) extends Logging {
  @Value("${wcmc.api.rest.dataformer.host:luna.fiehnlab.ucdavis.edu}")
  private val host: String = ""

  @Value("${wcmc.api.rest.dataformer.port:9090}")
  private val port: Int = 0

  @Value("${wcmc.api.rest.dataformer.storage:#{systemProperties['java.io.tmpdir']}}")
  private val storage: String = ""


  @Value("${wcmc.api.rest.dataformer.conversiontimeout:120}")
  private val conversionTimeout: Int = 0


  /**
    * required due to the us needed to specify timeouts and so cant use default template
    */
  val restTemplate: RestTemplate = new RestTemplate(getRequestFactory())

  /**
    * builds a custom request factory to avoid timeouts
    *
    * @return
    */
  private def getRequestFactory(): ClientHttpRequestFactory = {
    val factory: HttpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create()
      .setMaxConnTotal(200)
      .setMaxConnPerRoute(50)
      .build())

    factory.setReadTimeout(conversionTimeout * 1000)
    factory.setConnectTimeout(5 * 1000)
    factory.setConnectionRequestTimeout(5 * 1000)

    factory
  }

  protected def url = s"http://${host}:${port}"

  final def convert(filename: String, extension: String = "mzml"): Option[File] = {
    val data = doConvert(filename, extension)

    if (data.isDefined) {
      if (data.get.exists()) {
        data
      }
      else {
        evictCachedValue(filename)
        doConvert(filename, extension)
      }
    }
    else {
      None
    }
  }

  def evictCachedValue(filename: String) = {
    logger.warn(s"cache is no longer valid, evicted ${filename}")
  }

  /**
    * converts from the given file name, to an alternative format
    *
    * @param filename
    * @param extension
    * @return
    */
  def doConvert(filename: String, extension: String = "mzml"): Option[File] = {

    if (fserv4j.exists(filename)) {
      val file = fserv4j.load(filename)

      if (file.isEmpty) {
        throw new IOException(s"File ${filename} did not download correctly")
      } else {

        try {
          val upresponse = upload(file.get, filename)

          if (extension.equalsIgnoreCase("mzml")) {
            Option(download(filename, FileType.MZML))
          }
          else {
            throw new IOException(s"invalid extension provided: ${extension}")
          }

        } catch {
          case uex: UploadException =>
            throw new IOException(uex.getMessage, uex)
          case dex: DownloadException =>
            throw new IOException(dex.getMessage, dex)
        }
      }
    } else {
      None
    }
  }

  private def writeBytes(data: Stream[Byte], file: File): Unit = {
    val target = new BufferedOutputStream(new FileOutputStream(file))
    try data.foreach(target.write(_)) finally target.close()
  }

  private def upload(stream: InputStream, name: String): String = {
    try {
      val map = new LinkedMultiValueMap[String, AnyRef]
      map.add("file", new ByteArrayResource(IOUtils.toByteArray(stream), name) {
        override def getFilename = name
      })

      val headers = new HttpHeaders
      headers.setContentType(MediaType.MULTIPART_FORM_DATA)

      val requestEntity = new HttpEntity[LinkedMultiValueMap[String, AnyRef]](map, headers)

      logger.info(s"uploading ${name} to: $url/rest/conversion/upload")

      val result = restTemplate.exchange(s"$url/rest/conversion/upload", HttpMethod.POST, requestEntity, classOf[String])

      if (result.getStatusCode == HttpStatus.OK) {
        val uploadResp = result.getBody
        result.getBody
      } else {
        logger.warn(s"received result was: ${result}")
        throw new UploadException(result.toString)
      }
    }
    finally {
      stream.close()
    }
  }

  private def download(fileName: String, format: FileType): File = {
    val endpoint = s"$url/rest/conversion/download/${fileName.replaceAll(" ", "%20")}/${format.toString.toLowerCase}"
    logger.info(s"downloading ${format} version of ${fileName}")

    val downloadName = storage.concat(File.separator).concat(fileName.substring(0, fileName.indexOf(".")))
    val toDownload = new File(s"${downloadName}.${format.toString.toLowerCase()}")

    val out = new BufferedOutputStream(new FileOutputStream(toDownload))
    try {
      IOUtils.copy(new URL(endpoint).openStream(), out)
      out.flush()

      toDownload
    } finally {
      out.close()
    }

  }
}

class UploadException(message: String) extends Exception(message) {}

class DownloadException(message: String) extends IOException(message) {}

object FileType extends Enumeration {
  type FileType = Value
  val ABF, MZML, MZXML = Value
}


@Configuration
class DataFormerAutoConfiguration extends Logging {

  @Bean
  def dataform(delegatingResourceLoader: DelegatingResourceLoader): DataFormerClient = new DataFormerClient(delegatingResourceLoader)
}
