package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager, TestPropertySource}

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[RunnerTestConfig]))
@ActiveProfiles(Array("test", "carrot.lcms", "jenny.pos",
  "carrot.output.writer.aws", "file.source.eclipse"))
@TestPropertySource(properties = Array(
  "CARROT_SAMPLE:LipidsPos_PhV14_021_265096.mzml",
  "CARROT_METHOD:jenny_tribe | test | test | positive",
  "CARROT_MODE:lcms",
  "carrot.submitter:dpedrosa@ucdavis.edu"
))
@EnableConfigurationProperties
class TestJennySample extends WordSpec with ShouldMatchers with LazyLogging {

  @Autowired
  val runner: Runner = null

  @Autowired
  val stasis_cli: StasisService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "a runner" should {
    "have a stasis client" in {
      stasis_cli should not be null
    }

    "process a sample" in {
      runner.run()
    }

    "have results on aws" in {
      stasis_cli.getResults("LipidsPos_PhV14_021_265096") should not be null
    }
  }
}
