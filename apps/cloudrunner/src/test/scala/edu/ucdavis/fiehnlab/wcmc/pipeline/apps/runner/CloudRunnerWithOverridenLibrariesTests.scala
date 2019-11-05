package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.ResultResponse
import org.apache.logging.log4j.scala.Logging
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
@ActiveProfiles(Array("test",
  "carrot.lcms",
  "file.source.eclipse",
  "carrot.report.quantify.height",
  "carrot.processing.replacement.mzrt",
  "carrot.processing.peakdetection",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation",
  "carrot.output.storage.aws",
  "carrot.runner.required",
  "carrot.targets.dummy",
  "carrot.output.storage.converter.target",
  "carrot.output.storage.converter.sample"
))
@TestPropertySource(properties = Array(
  "CARROT_SAMPLE:B2a_TEDDYLipids_Neg_NIST001.mzml",
  "CARROT_METHOD:teddy | 6550 | test | negative",
  "CARROT_MODE:lcms",
  "carrot.submitter:fake@mymail.edu"
))
class CloudRunnerWithOverridenLibrariesTests extends WordSpec with Matchers with Logging {
  @Value("#{environment.CARROT_SAMPLE}}")
  val filename = ""

  @Autowired
  val stasis_cli: StasisService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "a runner" should {
    val sample = filename.split("\\.").head
    "have results on aws" in {
      try {
        val results: ResultResponse = stasis_cli.getResults(sample).get

        results should not be null
        results.injections.size() should be > 0
      } catch {
        case ex: HttpClientErrorException =>
          fail(ex)
      }
    }
  }
}
