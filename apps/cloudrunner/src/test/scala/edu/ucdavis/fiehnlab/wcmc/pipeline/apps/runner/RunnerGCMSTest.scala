package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager, TestPropertySource}

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("test", "carrot.binbase", "runner", "carrot.targets.dummy"))
@TestPropertySource(properties = Array(
  "CARROT_SAMPLE:180501dngsa32_1.txt",
  "CARROT_METHOD:Gerstel | LECO-GC-TOF | rtx5recal | positive",
  "CARROT_MODE:gcms",
  "carrot.submitter:dpedrosa@ucdavis.edu"
))
class RunnerGCMSTest extends WordSpec with Matchers with Logging {

  @Autowired
  val runner: Runner = null

  @Autowired
  val stasis_cli: StasisService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  " a runner" should {
    "load the required sample and" must {

      "have results on aws" ignore {
        stasis_cli.getResults("180501dngsa32_1") should not be null
      }
    }
  }
}
