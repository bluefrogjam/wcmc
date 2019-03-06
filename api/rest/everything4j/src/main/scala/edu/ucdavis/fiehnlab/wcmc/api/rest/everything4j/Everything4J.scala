package edu.ucdavis.fiehnlab.wcmc.api.rest.everything4j

import java.io._
import java.net.{URI, URL, URLEncoder}

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.loader.RemoteLoader
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.zeroturnaround.zip.ZipUtil

/**
  * Created by wohlgemuth on 10/10/17.
  */
class Everything4J(host: String = "luna.fiehnlab.ucdavis.edu", port: Int = 80, enableLookup: Boolean = true) extends RemoteLoader with Logging {

  /**
    * is a server allowed to use this one for lookup
    * functionality
    */
  override def isLookupEnabled(): Boolean = enableLookup

  @Autowired
  val objectMapper: ObjectMapper = null

  /**
    * returns the related resource or none
    *
    * @param name
    * @return
    */
  override def load(name: String): Option[InputStream] = {
    val url = s"http://${host}:${port}?s=${URLEncoder.encode(name, "UTF8")}&j=1&path_column=1"
    logger.info(s"load is checking url: ${url}")

    val data = objectMapper.readValue(new URL(url), classOf[Search]).results.filter(_.`type`.toLowerCase() == "file")

    if (data.isEmpty) {
      val folder = objectMapper.readValue(new URL(url), classOf[Search]).results.filter(_.`type`.toLowerCase() == "folder")

      if (folder.isEmpty) {
        logger.warn(s"we can't identify what type of resource '${name}' is...")
        None
      } else {
        logger.debug(s"${name} is a folder, compressing now.")
        val content = createZip(folder.head.name, folder.head.path)
        logger.debug("returning compressed stream...")
        Option(content)
      }
    } else {
      val encoded = s"${data.head.path.replaceAll("\\\\", "/").replaceAll("\\s", "%20").replaceAll(":", "%3A")}/${URLEncoder.encode(data.head.name, "UTF8").replaceAll("\\+", "%20")}"
      val uri = s"http://${host}:${port}/${encoded}"

      logger.debug(s"loading file from URI: ${uri}")
      val content = new URI(uri).toURL

      try {
        Option(content.openStream())
      }
      finally {
        logger.info(s"download completed for ${name}")
      }
    }
  }

  /**
    * creates a compressed stream from a folder
    */
  def createZip(name: String, dir: String): InputStream = {
    val temp = s"tmp/${name}"
    val tempFile = new File(temp)
    tempFile.mkdirs()

    logger.debug(s"recursion starts at: ${name}")

    recurse(dir.concat("/").concat(name), temp, orig = name)
    logger.debug(s"Compressed File source: ${tempFile.getAbsolutePath}")

    val zipFile = new File(s"tmp/${name}.zip")

    try {
      ZipUtil.pack(tempFile, zipFile)

      tempFile.toPath
    } catch {
      case ex: Exception =>
        logger.error(s"OOPS: ${ex.getMessage}", ex)
    }

    new FileInputStream(zipFile)
  }

  /**
    * creates a local copy of a .d file provided by "everything"
    *
    * @param start
    * @param orig
    */
  def recurse(start: String, tempDestFolder: String = "tmp", orig: String = ""): Option[String] = {

    val path = start.replaceAll("\\\\", "/").replaceAll("\\s", "%20") // hack to fix everything's duplicated 'size' field on folder request bug
    val queryStr = "j=1&path_column=1&size_column=0&date_modified_column=0&date_created_column=0&attributes_column=0"
    val url = new URL(s"http://${host}:${port}/${path}?${queryStr}")

    val search = objectMapper.readValue(url, classOf[Search])
    logger.debug(s"Search result: ${search.results}")

    search.results.foreach(t => {
      val goodPath = if (t.path != null) t.path else path

      t.`type`.toLowerCase() match {
        case "file" => //download file
          downloadFile(goodPath.concat("/").concat(t.name), new File(s"${tempDestFolder}/${t.name}"))
        case "folder" => {
          val dir = s"${if (t.path != null) t.path else path}/${t.name}"
          val sd = s"$tempDestFolder/${t.name}"
          new File(sd).mkdirs()
          recurse(dir, sd, s"${orig}/${t.name}")
        }
        case _ => None
      }
    })

    Option(tempDestFolder)
  }

  def downloadFile(source: String, dest: File, force: Boolean = false): Unit = {
    if (dest.exists()) {
      if (force) {
        logger.warn(s"deleting file: ${dest}")
        dest.delete()
      } else {
        logger.debug(s"reusing file: ${dest}")
        return
      }
    }

    val content = new URI(s"http://${host}:${port}/${source}").toURL
    logger.debug(s"Downloading file '${content.getFile}' to ${dest}")

    val outstr = new FileOutputStream(dest)

    try {
      IOUtils.copyLarge(content.openStream(), outstr)
    } finally {
      outstr.flush()
      outstr.close()
    }
  }

  /**
    * does the given resource exists
    *
    * @param name
    * @return
    */
  override def exists(name: String): Boolean = {
    val url = s"http://${host}:${port}?s=${URLEncoder.encode(name, "UTF8")}&j=1&path_column=1"
    logger.info(s"${this.getClass.getSimpleName} is checking url: ${new URL(url)}")

    val result = objectMapper.readValue(new URL(url), classOf[Search]).results.nonEmpty

    result
  }
}

@Configuration
class Everything4JConfiguration

case class Result(`type`: String, name: String, path: String, size: Long, created_date: Long)

case class Search(totalResults: Double, results: List[Result])
