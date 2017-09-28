package edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j

import java.io._

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.FileType.FileType
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.core.io.{FileSystemResource, InputStreamResource}
import org.springframework.http._
import org.springframework.http.client.{ClientHttpRequestFactory, HttpComponentsClientHttpRequestFactory}
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.{PathVariable, RequestParam}
import org.springframework.web.client.RestTemplate
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.ObjectMapper

/**
	* Created by diego on 8/30/2017.
	*
	* sends a raw data file to DataFormer rest service to be converted into .abf and .mzml and then sends the result to fserv
	*/
@Component
class DataFormerClient extends LazyLogging {
	@Value("${wcmc.api.rest.dataformer.host:128.120.143.101}")
	val host: String = ""

	@Value("${wcmc.api.rest.dataformer.port:9090}")
	val port: Int = 0

	@Autowired
	val fserv4j: FServ4jClient = null

	@Autowired
	val restTemplate: RestTemplate = null

	protected def url = s"http://${host}:${port}"

	def convert(filename: String): Map[String, String] = {
		var abfFile: File = null
		var mzmlFile: File = null
		var file: Option[InputStream] = null

        val mapper = new ObjectMapper

		if (fserv4j.exists(filename)) {
			file = fserv4j.download(filename)
		} else {
			Map("abf" -> null, "mzml" -> null, "error" -> s"Can't download raw file ${filename} from file server")
		}


		if (file.isEmpty) {
			Map("abf" -> null, "mzml" -> null)
		} else {
			val tmpfile = new File(filename)

			IOUtils.copyLarge(file.get, new FileOutputStream(tmpfile))

			try {
				val upresponse = upload(tmpfile)
				logger.info(s"uploaded msg: ${upresponse}")

                mapper.registerModule(DefaultScalaModule)
                val uploaded = mapper.readValue(upresponse, classOf[Map[String, String]])

                if(uploaded.getOrElse("abf","not found").equals("ok")) {
                    abfFile = download(tmpfile, FileType.ABF)
                    logger.debug(s"abfFile: ${abfFile.length()}")
                    fserv4j.upload(abfFile)
                    logger.debug(s"${abfFile} added to FileServer")
                } else {
                    logger.info("Could not convert to ABF")
                }

                if(uploaded.getOrElse("mzml","not found").equals("ok")) {
                    mzmlFile = download(tmpfile, FileType.MZML)
                    logger.debug(s"mzmlFile: ${mzmlFile.length()}")
                    fserv4j.upload(mzmlFile)
                    logger.debug(s"${mzmlFile} added to FileServer")
                } else {
                    logger.info("Could not convert to ABF")
                }

				Map("abf" -> abfFile.getName, "mzml" -> mzmlFile.getName)
			} catch {
				case uex: UploadException =>
					Map("abf" -> null, "mzml" -> null, "error" -> uex.getMessage)
				case dex: DownloadException =>
					Map("abf" -> null, "mzml" -> null, "error" -> dex.getMessage)
			}
		}
	}

	def writeBytes(data: Stream[Byte], file: File): Unit = {
		val target = new BufferedOutputStream(new FileOutputStream(file))
		try data.foreach(target.write(_)) finally target.close()
	}

	def upload(file: File): String = {
		val map = new LinkedMultiValueMap[String, AnyRef]
		map.add("file", new FileSystemResource(file))
		val headers = new HttpHeaders
		headers.setContentType(MediaType.MULTIPART_FORM_DATA)

		val requestEntity = new HttpEntity[LinkedMultiValueMap[String, AnyRef]](map, headers)

		logger.info(s"uploading ${file.getName} to: $url/rest/conversion/upload")
		logger.debug(s"params: $requestEntity")

		val result = restTemplate.exchange(s"$url/rest/conversion/upload", HttpMethod.POST, requestEntity, classOf[String])

		if(result.getStatusCode == HttpStatus.OK) {
			val uploadResp = result.getBody
			logger.info(s"filename = $uploadResp")
			result.getBody
		} else {
			logger.warn(s"received result was: ${result}")
			throw new UploadException(result.toString)
		}
	}

	def download(file: File, format: FileType): File = {
        val endpoint = s"$url/rest/conversion/download/${file}/${format.toString.toLowerCase}"
        logger.info(s"downloading ${format} version of ${file.getName}")

		val request: HttpEntity[String] = new HttpEntity("parameters")
		val result = restTemplate.exchange(endpoint, HttpMethod.GET, request, classOf[Array[Byte]])

		val downloadName = file.getName.substring(0, file.getName.indexOf("."))
		if (result.getStatusCode == HttpStatus.OK) {
			val toDownload = new File(s"${downloadName}.${format.toString.toLowerCase()}")

            logger.debug(s"Trying to download: ${toDownload}")
			val out = new BufferedOutputStream(new FileOutputStream(toDownload))
			IOUtils.copy(new ByteArrayInputStream(result.getBody), out)
			out.flush()
			out.close()

			toDownload
		} else {
			throw new DownloadException(result.toString)
		}
	}
}

class UploadException(message: String) extends Exception(message) {}
class DownloadException(message: String) extends IOException(message) {}

object FileType extends Enumeration {
	type FileType = Value
	val ABF, MZML = Value
}


@Configuration
class DataFormerConfiguration extends LazyLogging {
	@Value("${wcmc.api.rest.dataformer.conversiontimeout:30}")
	val conversionTimeout: Int = 0

	@Bean
	def restTemplate() : RestTemplate = new RestTemplate(getRequestFactory())

	private def getRequestFactory(): ClientHttpRequestFactory = {
		val factory: HttpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory()

		factory.setReadTimeout(conversionTimeout * 1000)
		factory.setConnectTimeout(1 * 1000)
		factory.setConnectionRequestTimeout(1 * 1000)
		factory
	}
}