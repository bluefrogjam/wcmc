package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.NegativeMode
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
import org.springframework.test.context.{ActiveProfiles, TestContextManager, TestPropertySource}


@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("test",
  "carrot.lcms",
  "file.source.eclipse",
  "carrot.report.quantify.height",
  "carrot.processing.peakdetection",
  "carrot.targets.yaml.annotation",
  "carrot.targets.yaml.correction"))
@TestPropertySource(properties = Array(
  "wcmc.workflow.lcms.process.correction.minPeakIntensity:5000"
))
class CorrectionStandardOrderBugFailTest extends WordSpec with Matchers with Logging {

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
    "have minPeakIntensity of 5000" in {
      correction.minPeakIntensity shouldBe 5000
    }

    "succeed with high intensity setting for standard" in {
      val sample = loader.getSample("FL95-032_Wk1_B4_posCSH_Keim_2.mzml")
      val method = AcquisitionMethod(ChromatographicMethod("keim", Some("6550"), Some("test"), Some(NegativeMode())))

      val corrected = correction.process(deco.process(sample, method, None), method, None)
      corrected.featuresUsedForCorrection.size should be >= correction.minimumFoundStandards
    }
  }
}
