package edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j

import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by wohlgemuth on 6/16/17.
  */
@RunWith(SpringRunner.class)
@SpringBootTest
class MSDialRestProcessorTest extends WordSpec with ShouldMatchers{

  @Autowired
  val mSDialRestProcessor:MSDialRestProcessor = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MSDialRestProcessorTest" should {

    "process" must {

      "process a .d file" in {

      }

      "process a .abf file" in {

      }

    }

  }
}
