package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.controller

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.utilities.minix.types.SampleInformationResult
import edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.Carrot
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.web.client.RestTemplate

@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Array(classOf[Carrot]))
@ActiveProfiles(Array("test"))
class IntegrationControllerTest extends WordSpec with Matchers with Logging {


  @LocalServerPort
  private val port: Int = 0

  @Autowired
  val template: RestTemplate = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "IntegrationControllerTest" should {

    "fetch LC MinixStudy" in {
      val result = template.getForObject(s"http://localhost:${port}/rest/integration/minix/357560", classOf[Seq[SampleInformationResult]])
      result.length shouldBe 1300
    }

    "fetch GC MinixStudy" in {
      val result = template.getForObject(s"http://localhost:${port}/rest/integration/minix/63618", classOf[Seq[SampleInformationResult]])
      result.length shouldBe 6
    }
  }
}
