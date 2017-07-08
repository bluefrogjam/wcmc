package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdial.MSDialSample
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.exception.NotEnoughStandardsFoundException
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing.PurityProcessing
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
  val purity: PurityProcessing = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "LCMSRetentionIndexCorrectionTest" should {

    val sample2 = new MSDialSample(getClass.getResourceAsStream("/lipids/B5_P20Lipids_Pos_NIST02.msdial"), "B5_P20Lipids_Pos_NIST02.msdial")
    val sample3 = new MSDialSample(getClass.getResourceAsStream("/lipids/B5_P20Lipids_Pos_QC000.msdial"), "B5_P20Lipids_Pos_QC000.msdial")

    assert(correction != null)

    "configure out settings " in {
      correction.correctionSettings.minimumFoundStandards = 16
    }

    "process will fail" must {


      s"because we don't have enough standards in ${sample3}" in {

        val error = intercept[NotEnoughStandardsFoundException] {
          correction.process(purity.process(sample3))
        }
        assert(error != null)
      }

      s"should have enoguth standards for us to continue ${sample2}" in {
        val corrected = correction.process(purity.process(sample2))

        assert(corrected.regressionCurve != null)
      }

    }


  }
}

