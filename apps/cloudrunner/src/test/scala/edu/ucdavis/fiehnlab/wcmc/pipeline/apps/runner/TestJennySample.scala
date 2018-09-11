package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client.StasisClient
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager, TestPropertySource}

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[RunnerTestConfig]))
@ActiveProfiles(Array("test", "carrot.lcms",
  "carrot.output.writer.aws", "file.source.eclipse",
  "file.source.luna", "carrot.resource.store.bucket"))
@TestPropertySource(properties = Array(
  "CARROT_SAMPLE:BioRec_LipidsPos_PhIV_001a.mzml",
  "CARROT_METHOD:jenny-tribe | test | test | positive",
  "CARROT_MODE:lcms",
  "carrot.submitter:dpedrosa@ucdavis.edu"
))
class TestJennySample extends WordSpec with ShouldMatchers with LazyLogging with BeforeAndAfter {

  @Value("#{CARROT_SAMPLE}")  // experimental, i'm getting 404 or 403 when calling stasis/result api
  val sample = ""

  @Autowired
  val runner: Runner = null

  @Autowired
  val stasis_cli: StasisClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "a runner" should {
    "have valid sample name" in {
      sample.split(".").head should equal("BioRec_LipidsPos_PhIV_001a")
    }

    "have a stasis client" in {
      println(stasis_cli.getClass.getSimpleName)
      stasis_cli should not be null
    }

    "process a sample" in {
      runner.run()
    }

    "have results on aws" in {
      stasis_cli.getResults(sample.split(".").head) should not be null
    }
  }
}
