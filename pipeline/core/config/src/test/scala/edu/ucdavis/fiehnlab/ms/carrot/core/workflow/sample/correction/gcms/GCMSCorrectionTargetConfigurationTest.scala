package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.gcms.GCMSAnnotationProperties
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(classOf[SpringJUnit4ClassRunner])
@ActiveProfiles(Array("carrot.gcms"))
@SpringBootTest
class GCMSCorrectionTargetConfigurationTest extends WordSpec with ShouldMatchers {

  @Autowired
  val library: LibraryAccess[Target] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "GCMSCorrectionTargetConfigurationTest" should {

    "correctionTargets" in {

    }

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class TestApplication