package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestPropertySource}

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("test",
  "carrot.binbase",
  "carrot.gcms",
  "carrot.report.quantify.height",
  "carrot.processing.replacement.mzrt",
  "carrot.processing.peakdetection",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation",
  "carrot.output.storage.aws",
  "carrot.runner.required",
  "carrot.targets.dummy",
  "file.source.eclipse"))
@TestPropertySource(properties = Array(
  "CARROT_SAMPLE:180501dngsa32_1.txt",
  "CARROT_METHOD:Gerstel | LECO-GC-TOF | rtx5recal | positive",
  "CARROT_MODE:gcms",
  "carrot.submitter:fake@mymail.edu"
))
class RunnerGCMSTest extends WordSpec with Matchers with Logging {
//
//  @Autowired
//  val runner: Runner = null
//
//  @Autowired
//  val stasis_cli: StasisService = null
//
//  new TestContextManager(this.getClass).prepareTestInstance(this)
//
//  " a runner" should {
//    "load the required sample and" must {
//
//      "have results on aws" ignore {
//        stasis_cli.getResults("180501dngsa32_1") should not be null
//      }
//    }
//  }
}
