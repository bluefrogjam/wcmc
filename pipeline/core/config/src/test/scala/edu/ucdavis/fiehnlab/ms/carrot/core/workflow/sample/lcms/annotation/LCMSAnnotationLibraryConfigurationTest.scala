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
@ActiveProfiles(Array("test", "carrot.lcms"))
@SpringBootTest
class LCMSAnnotationLibraryConfigurationTest extends WordSpec with ShouldMatchers with LazyLogging {
  @Autowired
  val annotProperties: LCMSAnnotationLibraryProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Default annotation library properties" should {
    "not have annotation targets" in {
      annotProperties.config shouldBe empty
    }
  }
}

@RunWith(classOf[SpringRunner])
@ActiveProfiles(Array("test", "carrot.lcms", "carrot.jenny"))
@SpringBootTest
class LCMSCustomAnnotationLibraryConfigurationTest extends WordSpec with ShouldMatchers with LazyLogging {
  @Autowired
  val annotProperties: LCMSAnnotationLibraryProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Custom annotation library properties" should {
    "have annotation targets" in {
      annotProperties.config should not be empty
    }
  }
}
