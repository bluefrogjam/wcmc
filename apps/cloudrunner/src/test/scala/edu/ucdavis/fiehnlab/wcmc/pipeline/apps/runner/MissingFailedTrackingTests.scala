package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager, TestPropertySource}

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("test", "carrot.lcms", "csh", "carrot.targets.dummy"))
@TestPropertySource(properties = Array(
  "CARROT_SAMPLE:LipidsPos_PhV1_06_160362.mzml",
  "CARROT_METHOD:csh | 6530 | test | positive",
  "CARROT_MODE:lcms",
  "carrot.submitter:dpedrosa@ucdavis.edu"
))
class MissingFailedTrackingTests extends WordSpec with Matchers with Logging {
  @Value("#{environment.CARROT_SAMPLE}")
  val sample = ""

  @Autowired
  val stasis_cli: StasisService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "the sample" should {
    "have all tracking statuses" ignore {
      val results = stasis_cli.getTracking(sample.split(".").head)

      logger.info(results.status.mkString("\n"))

      results.status.map {
        _.value
      } should contain("failed")
    }
  }
}

@Configuration
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
class MissingConfig
