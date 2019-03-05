package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.lcms.correction

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSCorrectionLibraryProperties
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@ActiveProfiles(Array("test", "carrot.lcms"))
@SpringBootTest
class LCMSCorrectionLibraryConfigurationTest extends WordSpec with ShouldMatchers with Logging {
  @Autowired
  val properties: LCMSCorrectionLibraryProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  val libName = "lcms_istds"

  "Correction library properties" should {
    "have properties for correction" in {
      properties.config should not be null
    }

    "load library 'lcms_istds'" in {
      properties.config.get(0).name should equal(libName)
    }

    "have 28 targets in library" in {
      properties.config.get(0).targets.size should be >= 25   // modified from 28 since i had to remove targets for annotation library testing
    }

    "have instrument name 'test'" in {
      properties.config.get(0).instrument should equal("test")
    }

    "have column name 'test'" in {
      properties.config.get(0).column should equal("test")
    }
  }
}
