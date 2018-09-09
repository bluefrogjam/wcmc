package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.lcms.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.AnnotationTarget
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.lcms.LCMSAnnotationLibraryProperties
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringJUnit4ClassRunner])
@ActiveProfiles(Array("test", "carrot.lcms"))
@SpringBootTest
class LCMSAnnotationPropertiesTest extends WordSpec with ShouldMatchers{

  @Autowired
  val lcmsAnnotationProperties: LCMSAnnotationLibraryProperties = null

  @Autowired
  val library:LibraryAccess[AnnotationTarget] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "we need to require at least one configuration" should {

    "have 1 config settings" in {
      lcmsAnnotationProperties.config.size() === 2
    }
  }
}
