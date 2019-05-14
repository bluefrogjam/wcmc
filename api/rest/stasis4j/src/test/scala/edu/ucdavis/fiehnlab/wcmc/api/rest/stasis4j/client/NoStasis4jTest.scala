package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client

import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@ActiveProfiles(Array("test", "carrot.nostasis"))
@SpringBootTest(classes = Array(classOf[StasisTestConfiguration]))
class NoStasis4jTest extends WordSpec with Matchers with Logging with Eventually {

  @Autowired
  val client: StasisService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "StasisService Integration Tests" should {

    "must be of type no stasis client" in {
      assert(client.isInstanceOf[NoOpStasisService])
    }
  }
}

