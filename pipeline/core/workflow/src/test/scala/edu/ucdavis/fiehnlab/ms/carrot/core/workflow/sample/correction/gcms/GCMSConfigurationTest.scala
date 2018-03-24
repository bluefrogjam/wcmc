package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}


@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("carrot.gcms"))
class GCMSConfigurationTest extends WordSpec with ShouldMatchers{

  @Autowired
  val properties:GCMSLibraryProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "we should be able to read" should {

    "a list of different target configurations" in {

      properties.config should not be (null)
      properties.config.size() should be (1)

    }
    "property required standards needs to be defined" in {
      properties.requiredStandards should not be(0)
      properties.requiredStandards should be (5)
    }
  }

}
