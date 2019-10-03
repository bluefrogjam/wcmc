package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager, TestPropertySource}

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
  "carrot.scheduler.local"))
@TestPropertySource(properties = Array(
  "CARROT_SAMPLE:Biorec001_posCSH_preFlenniken001.mzml",
  "CARROT_METHOD:csh | 6530 | test | positive",
  "CARROT_MODE:lcms",
  "carrot.submitter:fake@mymail.edu"
))
class RunnerLCMSTest extends WordSpec with Matchers with Logging {
  @Value("${wcmc.workflow.lcms.sample:#{environment.CARROT_SAMPLE}}")
  val sampleName: String = null

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
      stasis_cli.getResults(sampleName.split('.')(0)) should not be null
    }
  }
}
