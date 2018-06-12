package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.exception.NotEnoughStandardsFoundException
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
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
@ActiveProfiles(Array("carrot.processing.peakdetection", "carrot.lcms", "file.source.luna" /*, "carrot.logging.json.enable"*/))
class LCMSRetentionIndexCorrectionProcessTest extends WordSpec with LazyLogging {

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val deco: PeakDetection = null


  new TestContextManager(this.getClass).prepareTestInstance(this)

  "LCMSRetentionIndexCorrectionTest" should {

    val sample2 = loader.getSample("B5_P20Lipids_Pos_NIST02.mzml")
    val sample3 = loader.getSample("B5_P20Lipids_Pos_QC000.mzml")
    val method = AcquisitionMethod(ChromatographicMethod("lcms_istds", Some("test"), Some("test"), Some(PositiveMode())))

    s"should fail, because we don't have enough standards in ${sample3}" in {

      correction.asInstanceOf[LCMSTargetRetentionIndexCorrectionProcess].minimumFoundStandards = 20
      val error = intercept[NotEnoughStandardsFoundException] {
        correction.process(deco.process(sample3, method), method)
      }

      assert(error != null)
    }

    s"should pass, because we have enough standards for us to continue ${sample2}" in {
      correction.asInstanceOf[LCMSTargetRetentionIndexCorrectionProcess].minimumFoundStandards = 10

      val corrected = correction.process(deco.process(sample2, method), method)

      for (x <- corrected.featuresUsedForCorrection) {
        logger.info(s"used for correction: ${x.annotation.massOfDetectedFeature}")
      }

      assert(corrected.regressionCurve != null)
    }

  }
}
