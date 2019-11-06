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
  "carrot.targets.dummy",
  "carrot.output.storage.converter.target",
  "carrot.output.storage.converter.sample"
))
@TestPropertySource(properties = Array(
  "CARROT_SAMPLE:LipidsPos_PhV1_06_160362.mzml",
  "CARROT_METHOD:csh | 6530 | test | positive",
  "CARROT_MODE:lcms",
  "carrot.submitter:fake@mymail.edu"
))
class MissingFailedTrackingTests extends WordSpec with Matchers with Logging {
  @Value("#{environment.CARROT_SAMPLE}")
  val filename = ""

  @Autowired
  val stasis_cli: StasisService = null

  @Autowired
  val runner: Runner = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "the sample" should {
    val sample = filename.split("\\.").head

    "have all tracking statuses" in {
      val results = stasis_cli.getTracking(sample).get
      logger.info(s"Received: ${results}")
      results.status.map {
        _.value
      } should contain("exported")
    }
  }
}
