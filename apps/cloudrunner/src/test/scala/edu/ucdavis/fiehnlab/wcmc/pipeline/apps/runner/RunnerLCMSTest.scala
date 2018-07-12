package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client.StasisClient
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager, TestPropertySource}

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[RunnerTestConfig]))
@ActiveProfiles(Array("carrot.lcms", "carrot.output.writer.aws"))
@TestPropertySource(properties = Array(
  "carrot.sample:B5_P20Lipids_Pos_NIST01.mzml",
  "carrot.method:lcms_istds | test | test | positive"
))
class RunnerLCMSTest extends WordSpec with ShouldMatchers with LazyLogging {

  @Autowired
  val runner: Runner = null

  @Autowired
  val stasis_cli: StasisClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  " a runner" should {
    "load the required sample and" must {
      "process it" in {
        runner.run()
      }

      "have results on aws" in {
        stasis_cli.getResults("B5_P20Lipids_Pos_NIST01") should not be null
      }
    }
  }
}
