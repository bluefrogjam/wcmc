package edu.ucdavis.fiehnlab.cts3.server

import com.typesafe.scalalogging.LazyLogging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by diego on 01/17/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ConversionControllerIntegrationTest extends WordSpec with Matchers with LazyLogging {
  @Autowired
  val template: TestRestTemplate = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ConversionControllerIntegrationTest" should {

    "return hits as json" in {
      val result = template.getForObject("/rest/convert/{from}/{to}/{keyword}", classOf[String], "name", "inchikey", "alanine")

      result shouldEqual "[{\"keyword\":\"alanine\",\"from\":\"name\",\"to\":\"inchikey\",\"result\":\"InChIKey=QNAYBMKLOCPYGJ-REOHCLBHSA-N\",\"score\":1.0}]"
    }
  }
}
