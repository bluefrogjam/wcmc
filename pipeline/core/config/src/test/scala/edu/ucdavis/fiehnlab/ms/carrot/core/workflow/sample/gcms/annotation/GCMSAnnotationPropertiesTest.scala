package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.gcms.annotation

import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringJUnit4ClassRunner])
@ActiveProfiles(Array("carrot.gcms"))
@SpringBootTest
class GCMSAnnotationPropertiesTest extends WordSpec with ShouldMatchers{

  @Autowired
  val gCMSAnnotationProperties:GCMSAnnotationProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "we need to require at least one configuration" should {

    "have 1 config settings" in {
      gCMSAnnotationProperties.config.size() should be >= 1
    }
  }
}
@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class GCMSConfigurationTestApplication
