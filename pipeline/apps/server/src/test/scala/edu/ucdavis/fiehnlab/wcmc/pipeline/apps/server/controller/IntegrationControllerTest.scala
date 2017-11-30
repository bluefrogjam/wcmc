package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.controller

import edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.Carrot
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate

@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Array(classOf[Carrot]))
class IntegrationControllerTest extends WordSpec with ShouldMatchers {


  @LocalServerPort
  private val port: Int = 0

  @Autowired
  val template: RestTemplate = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "IntegrationControllerTest" should {

    "fetchMinixStudie" in {

      val result = template.getForObject(s"http://localhost:${port}/rest/integration/minix/63618", classOf[Array[Map[Any, Any]]])

      result.length shouldBe 6
    }

  }
}