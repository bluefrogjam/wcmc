package edu.ucdavis.fiehnlab.wcmc.apps.localrunner

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.NegativeMode
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.{ConfigFileApplicationContextInitializer, SpringBootTest}
import org.springframework.test.context.{ActiveProfiles, ContextConfiguration, TestContextManager}
import org.springframework.test.context.junit4.SpringRunner

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ContextConfiguration(initializers = Array(classOf[ConfigFileApplicationContextInitializer]))
@ActiveProfiles(Array(
  "test"
))
class LocalRunnerTest extends WordSpec {

  @Autowired
  val runner: Runner = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "LocalRunnerTest" should {

    "process" in {
      runner.process(Seq("B2b_SA1594_TEDDYLipids_Neg_MSMS_1U2WN.mzml"), AcquisitionMethod(ChromatographicMethod("teddy", Some("6550"), Some("test"), Option(NegativeMode()))))
    }

  }
}
