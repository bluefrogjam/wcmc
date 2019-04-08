package edu.ucdavis.fiehnlab.wcmc.server.fserv.controller

import java.io.{File, RandomAccessFile}

import edu.ucdavis.fiehnlab.wcmc.server.fserv.FServ
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.core.io.FileSystemResource
import org.springframework.http._
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.LinkedMultiValueMap

@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Array(classOf[FServ]))
class FailingTests extends WordSpec with Logging with Matchers {

  @Value("${wcmc.server.fserv.directory:target/test/storage}")
  val directory: String = null

  @LocalServerPort
  private val port: Int = 0

  val template = new TestRestTemplate()

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "FServControllerTest" should {
    val serverUrl = s"http://localhost:${port}/rest/file"

    "upload and download a simple file" must {
      logger.info(s"Source folder: ${directory}")
      logger.info(s"Storage folder: ${directory}")

      val temp = File.createTempFile("temp", "x")
      temp.deleteOnExit()
      val f = new RandomAccessFile(temp, "rw")
      f.setLength(1024 * 1024 * 250)
      f.close()

      "upload test file" in {

        val headers = new HttpHeaders
        headers.setContentType(MediaType.MULTIPART_FORM_DATA)

        val body = new LinkedMultiValueMap[String, AnyRef]
        body.add("file", new FileSystemResource(temp))
        body.add("name", temp.getName)

        val requestEntity = new HttpEntity[LinkedMultiValueMap[String, AnyRef]](body, headers)

        val response = template.exchange(s"${serverUrl}/upload", HttpMethod.POST, requestEntity, classOf[Any])

        logger.info(response.getStatusCode.getReasonPhrase)
        response.getStatusCode should be(HttpStatus.OK)
      }

      // failing after merging origin/binbase (06/06/18) since we need a fileserver with the correct file in it
      "download zipped agilent" in {
        val headers = new HttpHeaders
        headers.setAccept(java.util.Arrays.asList(MediaType.APPLICATION_OCTET_STREAM))

        val entity = new HttpEntity[String](headers)

        val response = template.exchange(s"${serverUrl}/download/${temp.getName}", HttpMethod.GET, entity, classOf[Array[Byte]])

        logger.info(response.getStatusCode.getReasonPhrase)
        response.getStatusCode should be(HttpStatus.OK)
      }
    }
  }
}
