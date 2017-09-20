package edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j

import java.io.{BufferedOutputStream, File, FileOutputStream}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.FileType.FileType
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.core.io.FileSystemResource
import org.springframework.http._
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

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
	val port: Int = 9090

	@Autowired
	val fserv4j: FServ4jClient = null

//	@Autowired
	val restTemplate: RestTemplate = new RestTemplate()

	protected def url = s"http://${host}:${port}"

	def convert(file: File): Map[String, File] = {
		var abfFile: File = null
		var mzmlFile: File = null

		logger.debug(s"processing file: ${file.getName}")
		try {
			var uploaded = upload(file)
			logger.debug(s"uploaded msg: ${uploaded}")

			abfFile = download(file, FileType.ABF)
			logger.debug(s"abfFile: ${abfFile.length()}")

			mzmlFile = download(file, FileType.MZML)
			logger.debug(s"mzmlFile: ${mzmlFile.length()}")


			Map("abf" -> abfFile, "mzml" -> mzmlFile)
		} catch {
			case ex: UploadException =>
			Map("abf" -> null, "mzml" -> null)
		}
	}


	def upload(file: File): String = {
		val map = new LinkedMultiValueMap[String, AnyRef]
		map.add("file", new FileSystemResource(file))
		val headers = new HttpHeaders
		headers.setContentType(MediaType.MULTIPART_FORM_DATA)

		val requestEntity = new HttpEntity[LinkedMultiValueMap[String, AnyRef]](map, headers)

		logger.debug(s"uploading to: $url/rest/upload")
		logger.debug(s"params: $requestEntity")

		val result = restTemplate.exchange(s"$url/rest/upload", HttpMethod.POST, requestEntity, classOf[String])

		if(result.getStatusCode == HttpStatus.OK) {
			val token = result.getBody
			logger.info(s"filename = $token")
			s"${file.getName} uploaded successfully"
		} else {
			logger.warn(s"received result was: ${result}")
			throw new UploadException(result.toString)
		}
	}

	def download(file: File, format: FileType): File = {
		val request: HttpEntity[String] = new HttpEntity("parameters")
		val result = restTemplate.exchange(s"$url/rest/conversion/${file.getName}/${format}", HttpMethod.GET, request, classOf[String])

		var downloadName = file.getName.substring(0, file.getName.lastIndexOf(".")-1)

		if (result.getStatusCode == HttpStatus.OK) {
			val toDownload = File.createTempFile(s"${downloadName}.", format.toString)

			val out = new BufferedOutputStream(new FileOutputStream(toDownload))
			out.write(result.asInstanceOf[Array[Byte]])
			out.flush()
			out.close()

			file
		} else {
			throw new DownloadException(result.toString)
		}
	}
}

class UploadException(message: String) extends Exception(message) {}
class DownloadException(message: String) extends Exception(message) {}

object FileType extends Enumeration {
	type FileType = Value
	val ABF, MZML = Value
}
