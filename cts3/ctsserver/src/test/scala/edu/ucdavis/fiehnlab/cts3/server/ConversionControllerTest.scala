package edu.ucdavis.fiehnlab.cts3.server

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.cts3.Cts
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, ComponentScan}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.{RestOperations, RestTemplate}

/**
  * Created by diego on 1/17/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[ControllerTestConfiguration]))
class ConversionControllerTest extends WordSpec with Matchers with LazyLogging {
  @Autowired
  val controller: ConversionController = null

  @Autowired
  val template: RestOperations = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ConversionControllerTest" should {

    "return a json string with hits" in {
      val result = controller.convert("name", "inchikey", "alanine")

      result shouldBe a[String]
      result shouldEqual "[{\"keyword\":\"alanine\",\"from\":\"name\",\"to\":\"inchikey\",\"result\":\"InChIKey=QNAYBMKLOCPYGJ-REOHCLBHSA-N\",\"score\":1.0}]"
    }

    "return an empty sequence of hits" in {
      val result = controller.convert("name", "inchikey", "doesn't exist")

      result shouldBe a[String]
      result shouldEqual "[]"
    }
  }
}

@SpringBootApplication
@ComponentScan(basePackageClasses = Array(classOf[Cts]))
class ControllerTestConfiguration {
  @Bean
  def template: RestOperations = new RestTemplate()
}