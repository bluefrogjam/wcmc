package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.ResultStorage
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager, TestPropertySource}
import org.springframework.web.client.HttpClientErrorException

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("test",
  "carrot.lcms",
  "csh",
  "file.source.eclipse",
  "carrot.report.quantify.height",
  "carrot.processing.replacement.mzrt",
  "carrot.processing.peakdetection",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation",
  "carrot.output.storage.aws",
  "carrot.output.writer.json",
  "carrot.output.storage.generic",
  "carrot.runner.required",
  "carrot.targets.dummy",
  "carrot.output.storage.converter.target",
  "carrot.output.storage.converter.sample",
  "carrot.resource.store.bucket"
))
@TestPropertySource(properties = Array(
  "CARROT_SAMPLE:BioRec_LipidsPos_PhIV_001a.mzml",
  "CARROT_METHOD:csh | 6530 | test | positive",
  "CARROT_MODE:lcms",
  "carrot.submitter:fake@mymail.edu"
))
class CloudRunnerWithDynamicLibrariesTests extends WordSpec with Matchers with Logging {
  @Value("${wcmc.workflow.lcms.sample:#{environment.CARROT_SAMPLE}}")
  val sampleName = ""

  @Autowired
  val runner: Runner = null

  @Autowired
  val storage: ResultStorage = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "a runner" should {
    "have results on aws" in {
      try {
        fail()
      } catch {
        case ex: HttpClientErrorException =>
          fail(ex)
      }
    }
  }
}
