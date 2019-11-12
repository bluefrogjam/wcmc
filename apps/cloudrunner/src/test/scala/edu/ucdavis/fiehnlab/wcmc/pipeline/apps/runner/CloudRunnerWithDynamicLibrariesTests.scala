package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import edu.ucdavis.fiehnlab.loader.{ResourceLoader, ResourceStorage}
import edu.ucdavis.fiehnlab.ms.carrot.cloud.bucket.{BucketLoader, BucketStorage}
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
  "carrot.resource.loader.bucket",
  "carrot.resource.store.bucket",
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
  val loader: BucketLoader = null

  @Autowired
  val storage: BucketStorage = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "a runner" should {
    "have results on aws" in {
      try {
        loader.exists(filename.split('.').head) should be(true)

        val data = loader.loadAsFile(filename.split('.').head)
        logger.info(s"DATA: ${data}")
        data.size should be > 0

      } catch {
        case ex: HttpClientErrorException =>
          fail(ex)
      } finally {
//        storage.delete(filename.split('.').head)
      }
    }
  }
}
