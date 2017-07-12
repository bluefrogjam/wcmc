package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import com.typesafe.scalalogging.LazyLogging
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
class LCMSRetentionIndexCorrectionTest extends WordSpec with LazyLogging{

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrection = null

  @Autowired
  val loader:SampleLoader = null


  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "LCMSRetentionIndexCorrectionTest" should {

    val sample2 = loader.getSample("B5_P20Lipids_Pos_NIST02.abf")
    val sample3 = loader.getSample("B5_P20Lipids_Pos_QC000.abf")

    assert(correction != null)


      s"should fail, because we don't have enough standards in ${sample3}" in {

        correction.minimumFoundStandards = 20
        val error = intercept[NotEnoughStandardsFoundException] {
          val result = correction.process(sample3)

          for(x <- result.annotationsUsedForCorrection ){
            logger.info(s"used for correction: ${x}")
          }
        }
        assert(error != null)
      }

      s"should pass, because we have enough standards for us to continue ${sample2}" in {
        correction.minimumFoundStandards = 17

        val corrected = correction.process(sample2)

        for(x <- corrected.annotationsUsedForCorrection ){
          logger.info(s"used for correction: ${x}")
        }

        assert(corrected.regressionCurve != null)
      }



  }
}

