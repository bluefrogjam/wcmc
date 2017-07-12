package edu.ucdavis.fiehnlab.server.fserv

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.{LocalLoader, ResourceLoader}
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.core.io.ClassPathResource
import org.springframework.http._
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.LinkedMultiValueMap
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.ByteArrayHttpMessageConverter
import java.nio.file.Files
import java.nio.file.Paths
import java.util

import scala.io.Source

/**
  * Created by wohlgemuth on 7/7/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class FServControllerTest extends WordSpec with LazyLogging with ShouldMatchers {
  @Value("${wcms.server.fserv.directory:storage}")
  val directory: String = null

  @LocalServerPort
  private val port: Int = 0

  val template = new TestRestTemplate()

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "FServControllerTest" should {

    "upload and download a simple file" must {

      "upload" in {
        val map = new LinkedMultiValueMap[String, AnyRef]
        map.add("file", new ClassPathResource("/test.txt"))
        val headers = new HttpHeaders
        headers.setContentType(MediaType.MULTIPART_FORM_DATA)

        val requestEntity = new HttpEntity[LinkedMultiValueMap[String, AnyRef]](map, headers)
        val result = template.exchange(s"http://localhost:${port}/rest/upload", HttpMethod.POST, requestEntity, classOf[java.util.Map[String,_  <: Any]])

        result.getStatusCode should be(HttpStatus.OK)

        result.getBody.get("TotalBytesRead").toString.toInt should be(9)

        //ensure the file was created
        new File(s"${directory}/test.txt").exists() should be(true)

        Source.fromFile(new File(s"${directory}/test.txt")).getLines().toSeq.size should be(Source.fromInputStream(new ClassPathResource("/test.txt").getInputStream).getLines().toSeq.size)


      }
      "exists" in {
        val headers = new HttpHeaders
        headers.setContentType(MediaType.APPLICATION_JSON)
        val entity = new HttpEntity[String](headers)
        val response = template.exchange(s"http://localhost:${port}/rest/exists/test.txt",HttpMethod.GET,entity, classOf[java.util.Map[String,Any]])
        response.getStatusCode should be(HttpStatus.OK)

      }
      "not exists" in {
        val response = template.getForEntity(s"http://localhost:${port}/rest/exists/test123.txt", classOf[Any])
        response.getStatusCode should be(HttpStatus.NOT_FOUND)
      }
      "not exists a complicated file name" in {
        val response = template.getForEntity(s"http://localhost:${port}/rest/exists/test_withUnder_and2extesnions.mzXML.gz", classOf[Any])
        println(response)
        response.getStatusCode should be(HttpStatus.NOT_FOUND)

      }

      "download" in {
        val headers = new HttpHeaders
        headers.setAccept(util.Arrays.asList(MediaType.APPLICATION_OCTET_STREAM))

        val entity = new HttpEntity[String](headers)

        val response = template.exchange(s"http://localhost:${port}/rest/download/test.txt", HttpMethod.GET, entity, classOf[Array[Byte]])

        response.getStatusCode should be(HttpStatus.OK)

        Files.write(Paths.get("target/test.txt.result"), response.getBody)

        new File("target/test.txt.result").exists() should be(true)

      }
    }


  }
}

@Configuration
class TestConfiguration {

  @Bean
  def resourceLoader: LocalLoader = new RecursiveDirectoryResourceLoader(new File("target"))
}