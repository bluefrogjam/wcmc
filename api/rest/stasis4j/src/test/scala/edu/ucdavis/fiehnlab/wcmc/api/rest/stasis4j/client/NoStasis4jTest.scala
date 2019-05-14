package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client

import java.util.Date

import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model._
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.web.client.HttpClientErrorException

import scala.collection.JavaConverters._

@RunWith(classOf[SpringRunner])
@ActiveProfiles(Array("test", "no-stasis"))
@SpringBootTest(classes = Array(classOf[StasisTestConfiguration]))
class NoStasis4jTest extends WordSpec with Matchers with Logging with Eventually {

  @Autowired
  val client: StasisClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "StasisClient Integration Tests" should {

    "must be of type no stasis client" in {
      assert(client.isInstanceOf[NoOpStasisClient])
    }
  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class StasisTestConfiguration
