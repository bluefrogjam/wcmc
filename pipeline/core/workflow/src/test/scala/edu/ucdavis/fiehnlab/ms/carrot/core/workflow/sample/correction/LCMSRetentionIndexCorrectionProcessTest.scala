package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.exception.NotEnoughStandardsFoundException
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.CorrectedSpectra
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlgemuth on 6/17/16.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("test",
  "carrot.lcms",
  "file.source.eclipse",
  "carrot.processing.peakdetection",
  "carrot.report.quantify.height",
  "carrot.targets.yaml.annotation",
  "carrot.targets.yaml.correction"))
class LCMSRetentionIndexCorrectionProcessTest extends WordSpec with Matchers with Logging {
  val libName = "lcms_istds"

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val deco: PeakDetection = null

  @Autowired
  val stasis_cli: StasisService = null


  new TestContextManager(this.getClass).prepareTestInstance(this)

  "LCMSRetentionIndexCorrectionTest" should {

    val method = AcquisitionMethod(ChromatographicMethod(libName, Some("test"), Some("test"), Some(PositiveMode())))

    s"should fail, because we don't have enough standards in B5_P20Lipids_Pos_QC000.mzml" in {
      val sample3 = loader.getSample("B5_P20Lipids_Pos_QC000.mzml")

      correction.asInstanceOf[LCMSTargetRetentionIndexCorrectionProcess].minimumFoundStandards = 20
      val error = intercept[NotEnoughStandardsFoundException] {
        correction.process(deco.process(sample3, method, None), method, None)
      }

      assert(error != null)
    }

    s"should pass, because we have enough standards for us to continue B5_P20Lipids_Pos_NIST02.mzml" in {
      val sample2 = loader.getSample("B5_P20Lipids_Pos_NIST02.mzml")
      correction.asInstanceOf[LCMSTargetRetentionIndexCorrectionProcess].minimumFoundStandards = 10

      val corrected = correction.process(deco.process(sample2, method, None), method, None)

      for (x <- corrected.featuresUsedForCorrection) {
        logger.info(s"used for correction: ${x.annotation.massOfDetectedFeature}")
      }

      assert(corrected.regressionCurve != null)
      assert(corrected.featuresUsedForCorrection.forall(x =>
        x.annotation.isInstanceOf[CorrectedSpectra] &&
        x.annotation.asInstanceOf[CorrectedSpectra].retentionIndex > 0
      ))

      stasis_cli.getTracking(sample2.name).get.status.map(_.value) should contain("deconvoluted")
      stasis_cli.getTracking(sample2.name).get.status.map(_.value) should contain("corrected")
    }

  }
}
