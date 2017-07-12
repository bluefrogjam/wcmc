package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.exception.NotEnoughStandardsFoundException
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlgemuth on 6/17/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("common"))
class LCMSRetentionIndexCorrectionTest extends WordSpec {

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrection = null

  @Autowired
  val loader:SampleLoader = null


  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "LCMSRetentionIndexCorrectionTest" should {

    val sample2 = loader.getSample("B5_P20Lipids_Pos_NIST02.mzML")
    val sample3 = loader.getSample("B5_P20Lipids_Pos_QC000.mzML")

    assert(correction != null)


      s"should fail, because we don't have enough standards in ${sample3}" in {

        val error = intercept[NotEnoughStandardsFoundException] {
          correction.process(sample3)
        }
        assert(error != null)
      }

      s"should pass, because we have enough standards for us to continue ${sample2}" in {
        val corrected = correction.process(sample2)

        assert(corrected.regressionCurve != null)
      }



  }
}

