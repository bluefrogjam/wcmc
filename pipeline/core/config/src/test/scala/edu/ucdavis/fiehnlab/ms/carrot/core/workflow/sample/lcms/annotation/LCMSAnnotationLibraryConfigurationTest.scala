package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.lcms.annotation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.lcms.LCMSAnnotationLibraryProperties
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@ActiveProfiles(Array("carrot.lcms"))
@SpringBootTest
class LCMSAnnotationLibraryConfigurationTest extends WordSpec with ShouldMatchers with LazyLogging {
  @Autowired
  val properties: LCMSAnnotationLibraryProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Correction library properties" should {
    "have properties for correction" in {
      properties.config should not be null
    }

    "load library 'lcms_istds'" in {
      properties.config.get(0).name should equal("lcms_istds")
    }

    "have 28 targets in library" in {
      properties.config.get(0).targets should have size 28
    }

    "have instrument name 'test'" in {
      properties.config.get(0).instrument should equal("test")
    }

    "have column name 'test'" in {
      properties.config.get(0).column should equal("test")
    }
  }
}
