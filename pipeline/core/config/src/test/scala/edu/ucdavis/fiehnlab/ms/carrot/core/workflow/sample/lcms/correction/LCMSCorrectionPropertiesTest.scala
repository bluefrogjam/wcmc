package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.lcms.correction

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.CorrectionTarget
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSCorrectionLibraryProperties
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringJUnit4ClassRunner])
@ActiveProfiles(Array("test", "carrot.lcms"))
@SpringBootTest
class LCMSCorrectionPropertiesTest extends WordSpec with Matchers{

  @Autowired
  val lcmsAnnotationProperties: LCMSCorrectionLibraryProperties = null

  @Autowired
  val library:LibraryAccess[CorrectionTarget] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "we need to require at least one configuration" should {

    "have 1 config settings" in {
      lcmsAnnotationProperties.config.size() should be >= 1
    }
  }
}
