package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.lcms.annotation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.lcms.LCMSAnnotationLibraryProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSCorrectionLibraryProperties
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.collection.JavaConverters._

@RunWith(classOf[SpringRunner])
@ActiveProfiles(Array("test", "carrot.lcms"))
@SpringBootTest
class LCMSAnnotationLibraryConfigurationTest extends WordSpec with ShouldMatchers with LazyLogging {
  @Autowired
  val annotProperties: LCMSAnnotationLibraryProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Correction library properties" should {
    "have properties for correction" in {
      annotProperties.config should not be null
    }

    "have annotation libraries" in {
      annotProperties.config.size() === 2
    }

    "have librarie 'jenny_tribe'" in {
      println(s"libraries: ${annotProperties.config.size()}")
      println(annotProperties.config.asScala.map(it => s"annotation ${it.name} | ${it.instrument} | ${it.column} | ${it.ionMode}").mkString("; "))
      annotProperties.config.asScala.map(_.name) should have size 2
    }

    "have over 1000 targets in each library (positive, negative)" in {
      annotProperties.config.get(0).targets.size() should be > 1000
      annotProperties.config.get(1).targets.size() should be > 1000
    }

    "have instrument name 'test'" in {
      annotProperties.config.get(0).instrument should equal("test")
    }

    "have column name 'test'" in {
      annotProperties.config.get(0).column should equal("test")
    }
  }
}
