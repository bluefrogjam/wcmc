package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.gcms.correction

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms.GCMSCorrectionLibraryProperties
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringJUnit4ClassRunner])
@ActiveProfiles(Array("carrot.gcms", "test"))
@SpringBootTest
class GCMSConfigurationTest extends WordSpec with Matchers {

  @Autowired
  val properties: GCMSCorrectionLibraryProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "we should be able to read" should {

    "a list of different target configurations" in {

      properties.config should not be (null)
      properties.config.size() should be(1)

    }
    "property required standards needs to be defined" in {
      properties.requiredStandards should be(5)
    }

    "property config 0 needs 13 standard" in {
      properties.config.get(0).targets.size() should be(13)
    }

    "should be in nominal mass mode" in {
      properties.config.get(0).isNominal() should be(true)
    }

    "have distance ratios" in {
      properties.config.get(0).targets.get(0).distanceRatios.size() should be(1)
    }

    "must be a validation target" in {
      properties.config.get(0).targets.get(0).validationTarget should be(true)
    }

    "must not be a validation target" in {
      properties.config.get(0).targets.get(12).validationTarget should be(false)
    }

    "must have the correct instrument" in {
      properties.config.get(0).instrument should be("LECO-GC-TOF")
    }


  }

}
