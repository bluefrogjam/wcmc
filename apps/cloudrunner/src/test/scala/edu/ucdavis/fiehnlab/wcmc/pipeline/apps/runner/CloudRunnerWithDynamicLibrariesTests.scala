package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import org.apache.logging.log4j.scala.Logging
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
@ActiveProfiles(Array("test", "carrot.lcms", "runner", "csh"))
@TestPropertySource(properties = Array(
  "CARROT_SAMPLE:BioRec_LipidsPos_PhIV_001a.mzml",
  "CARROT_METHOD:csh | 6530 | test | positive",
  "CARROT_MODE:lcms",
  "carrot.submitter:linuxmant@gmail.com",
  "mona.rest.server.user:admin",
  "mona.rest.server.password:admin",
  "mona.rest.server.host:ipa.fiehnlab.ucdavis.edu",
  "mona.rest.server.port:9090"
))
class CloudRunnerWithDynamicLibrariesTests extends WordSpec with ShouldMatchers with Logging {
  @Value("${carrot.sample:#{environment.CARROT_SAMPLE}}")
  val sampleName = ""

  @Autowired
  val runner: Runner = null

  @Autowired
  val stasis_cli: StasisService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "a runner" should {
    "have results on aws" in {
      try {
        val results = stasis_cli.getResults(sampleName.split('.').head)
        logger.info(results.toString)

        results should not be null
      } catch {
        case ex: HttpClientErrorException =>
          fail(ex)
      }
    }
  }
}
