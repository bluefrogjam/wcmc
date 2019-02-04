package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager, TestPropertySource}
import org.springframework.web.client.HttpClientErrorException

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("test", "carrot.lcms", "runner"))
@TestPropertySource(properties = Array(
  "CARROT_SAMPLE:B5_P20Lipids_Pos_NIST01.mzml",
  "CARROT_METHOD:csh | 6530 | test | positive",
  "CARROT_MODE:lcms",
  "carrot.submitter:dpedrosa@ucdavis.edu"
))
class RunnerLCMSTest extends WordSpec with ShouldMatchers with LazyLogging {
  @Value("${carrot.sample:#{environment.CARROT_SAMPLE}}")
  val sampleName = ""

  @Autowired
  val runner: Runner = null

  @Autowired
  val stasis_cli: StasisService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "a runner" should {
    "have a stasis client" in {
      stasis_cli should not be null
    }

    "have results on aws" in {
      try {
        stasis_cli.getResults(sampleName.split('.')(0)) should not be null
      } catch {
        case ex: HttpClientErrorException =>
          fail(ex)
      }
    }
  }
}
