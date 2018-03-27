package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.gcms.GCMSTargetRetentionIndexCorrection
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest
@ActiveProfiles(Array("carrot.gcms","carrot.gcms.correction"))
class GCMSTargetRetentionIndexCorrectionTest extends WordSpec with ShouldMatchers{

  @Autowired
  val correction:GCMSTargetRetentionIndexCorrection = null;


  new TestContextManager(this.getClass()).prepareTestInstance(this)


  "a gcms target correction" must {
    "find the retention index standard" should {

      "and not fail" in {

      }
    }
  }
}
