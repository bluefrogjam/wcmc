package edu.ucdavis.fiehnlab.wcmc.api.rest.everything4j

import com.typesafe.scalalogging.LazyLogging
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterEach, ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by wohlgemuth on 10/10/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TestConfig]))
class Everything4JTest extends WordSpec  with ShouldMatchers with BeforeAndAfterEach with LazyLogging{

  @Autowired
  val everything4J:Everything4J = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Everything4JTest" should {

    "load" in {
      everything4J.load("090309bsesa100_1.cdf").isDefined shouldBe true
    }

    "exists" must {

      "have file " in {
        everything4J.exists("090309bsesa100_1.cdf") shouldBe true
      }

      "have not file " in {
        everything4J.exists("090309bsesa100_1.cdf.D") shouldBe false
      }


    }

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class TestConfig{

}