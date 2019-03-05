package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.CorrectionTarget
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.{SpringJUnit4ClassRunner, SpringRunner}
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@ActiveProfiles(Array("carrot.lcms", "test"))
@SpringBootTest
class LCMSCorrectionTargetConfigurationTest extends WordSpec with Matchers {

  @Autowired
  val library: LibraryAccess[CorrectionTarget] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "LCMSCorrectionTargetConfigurationTest" should {

    "have several libraies" in {
      library.libraries.size should be >= 2
    }

  }
}
