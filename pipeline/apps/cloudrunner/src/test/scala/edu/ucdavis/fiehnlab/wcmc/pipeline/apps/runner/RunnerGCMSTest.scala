package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client.StasisClient
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager, TestPropertySource}

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[RunnerTestConfig]))
@ActiveProfiles(Array("carrot.binbase", "carrot.output.storage.aws"))
@TestPropertySource(properties = Array(
  "carrot.sample:180501dngsa32_1.txt",
  "carrot.method:Gerstel | LECO-GC-TOF | rtx5recal | positive"
))
class RunnerGCMSTest extends WordSpec with ShouldMatchers with LazyLogging {

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
        stasis_cli.getResults("180501dngsa32_1") should not be null
      }
    }
  }
}
