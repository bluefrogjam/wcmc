package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.gcms.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.CorrectionTarget
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.gcms.GCMSAnnotationProperties
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@ActiveProfiles(Array("carrot.gcms", "test"))
@SpringBootTest
class GCMSAnnotationPropertiesTest extends WordSpec with Matchers{

  @Autowired
  val gCMSAnnotationProperties:GCMSAnnotationProperties = null

  @Autowired
  val library:LibraryAccess[CorrectionTarget] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "we need to require at least one configuration" should {

    "have 1 config settings" in {
      gCMSAnnotationProperties.config.size() should be >= 1
    }
  }
}
