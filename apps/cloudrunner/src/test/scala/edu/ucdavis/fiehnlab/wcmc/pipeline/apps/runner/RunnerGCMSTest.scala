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
@SpringBootTest
@ActiveProfiles(Array("test", "carrot.binbase", "runner"))
@TestPropertySource(properties = Array(
  "CARROT_SAMPLE:180501dngsa32_1.txt",
  "CARROT_METHOD:Gerstel | LECO-GC-TOF | rtx5recal | positive",
  "CARROT_MODE:gcms",
  "carrot.submitter:dpedrosa@ucdavis.edu"
))
class RunnerGCMSTest extends WordSpec with ShouldMatchers with LazyLogging {

  @Autowired
  val runner: Runner = null

  @Autowired
  val stasis_cli: StasisClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  " a runner" should {
    "load the required sample and" must {

      "have results on aws" ignore {
        stasis_cli.getResults("180501dngsa32_1") should not be null
      }
    }
  }
}
