package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.CorrectionTarget
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.GCMSTestsConfiguration
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@ActiveProfiles(Array("test", "carrot.gcms"))
@SpringBootTest(classes = Array(classOf[GCMSTestsConfiguration]))
class GCMSCorrectionTargetConfigurationTest extends WordSpec with Matchers {

  @Autowired
  val library: LibraryAccess[CorrectionTarget] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "GCMSCorrectionTargetConfigurationTest" should {

    "must have libraries defined" in {
      library.libraries.size should be(1)
    }

  }
}
