package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import edu.ucdavis.fiehnlab.loader.ResourceStorage
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
  "file.source.eclipse",
  "carrot.report.quantify.height",
  "carrot.processing.replacement.mzrt",
  "carrot.processing.peakdetection",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation",
  "carrot.targets.dummy",
  "carrot.runner.required",
  "carrot.resource.loader.bucket.data",
  "carrot.resource.store.bucket.result",
  "carrot.output.storage.aws",
  "carrot.output.writer.json",
  "carrot.output.storage.converter.target",
  "carrot.output.storage.converter.sample"
))
@TestPropertySource(properties = Array(
  "CARROT_SAMPLE:BioRec_LipidsPos_PhIV_001a.mzml",
  "CARROT_METHOD:csh | 6530 | test | positive",
  "CARROT_MODE:lcms",
  "carrot.submitter:fake@mymail.edu"
))
class CloudRunnerWithDynamicLibrariesTests extends WordSpec with Matchers with Logging {
  @Value("#{environment.CARROT_SAMPLE}")
  val filename = ""

  @Autowired
  val storage: ResourceStorage = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "a runner" should {
    "use correct destination" in {
      storage.getDestination should equal("wcmc-data-stasis-result-test")
    }

    "have results on aws" in {
      val outputFile = filename.replace(".mzml", ".json")
      try {
        storage.exists(outputFile) should be(true)
      } catch {
        case ex: HttpClientErrorException =>
          fail(ex)
      } finally {
        try {
          storage.delete(outputFile)
        } catch {
          case ex2: Exception =>
            logger.error(s"Can't delete file $outputFile. ${ex2.getMessage}")
        }
      }
    }
  }
}
