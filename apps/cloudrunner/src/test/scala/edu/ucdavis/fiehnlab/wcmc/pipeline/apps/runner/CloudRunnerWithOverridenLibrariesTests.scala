package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager, TestPropertySource}
import org.springframework.web.client.HttpClientErrorException

/**
  * Created by diego on 11/6/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("test", "carrot.lcms", "runner", "csh"))
@TestPropertySource(properties = Array(
  "CARROT_SAMPLE:B2a_TEDDYLipids_Neg_NIST001.mzml",
  "CARROT_METHOD:csh | 6550 | test | negative",
  "CARROT_MODE:lcms",
  "carrot.submitter:linuxmant@gmail.com"
))
class CloudRunnerWithOverridenLibrariesTests extends WordSpec with Matchers with LazyLogging {
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
        val results = stasis_cli.getResults(sampleName.split('.')(0))
        logger.info(results.toString)

        results should not be null
      } catch {
        case ex: HttpClientErrorException =>
          fail(ex)
      }
    }
  }
}
