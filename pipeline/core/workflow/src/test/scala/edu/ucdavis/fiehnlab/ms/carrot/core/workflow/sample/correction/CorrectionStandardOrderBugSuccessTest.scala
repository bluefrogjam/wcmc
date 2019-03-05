package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by diego on 11/6/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("file.source.luna", "quantify-by-scan", "carrot.processing.peakdetection", "carrot.lcms", "test", "keim"))
class CorrectionStandardOrderBugSuccessTest extends WordSpec with ShouldMatchers with Logging {

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val deco: PeakDetection = null

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val stasis_cli: StasisService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Correction process" should {
    val sample = loader.getSample("FL95-032_Wk1_B4_posCSH_Keim_2.mzml")
    val method = AcquisitionMethod(ChromatographicMethod("keim", Some("6550"), Some("test"), Some(NegativeMode())))


    "have minPeakIntensity of 10000" in {
      correction.minPeakIntensity shouldBe 10000
    }

    "should succeed with high intensity setting for standard" in {
      val corrected = correction.process(deco.process(sample, method), method)
      corrected.featuresUsedForCorrection.size should be >= correction.minimumFoundStandards
    }
  }
}
