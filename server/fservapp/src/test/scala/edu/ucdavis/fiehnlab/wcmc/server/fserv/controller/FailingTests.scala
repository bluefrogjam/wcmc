package edu.ucdavis.fiehnlab.wcmc.server.fserv.controller

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.server.fserv.FServ
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http._
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Array(classOf[FServ]))
class FailingTests extends WordSpec with LazyLogging with ShouldMatchers {

  @Value("${wcmc.server.fserv.directory:storage}")
  val directory: String = null

  @LocalServerPort
  private val port: Int = 0

  val template = new TestRestTemplate()

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "FServControllerTest" should {

    "upload and download a simple file" must {

      // failing after merging origin/binbase (06/06/18) since we need a fileserver with the correct file in it
      "download zipped agilent" ignore {
        val headers = new HttpHeaders
        headers.setAccept(java.util.Arrays.asList(MediaType.APPLICATION_OCTET_STREAM))

        val entity = new HttpEntity[String](headers)
        val response = template.exchange(s"http://localhost:${port}/rest/file/download/B5_P20Lipids_Pos_QC000.d.zip", HttpMethod.GET, entity, classOf[Array[Byte]])

        response.getStatusCode should be(HttpStatus.OK)
      }
    }
  }
}
