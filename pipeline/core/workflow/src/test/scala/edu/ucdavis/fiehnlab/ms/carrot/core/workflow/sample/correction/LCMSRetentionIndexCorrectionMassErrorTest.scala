package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.MassAccuracy
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.NegativeMode
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.AccurateMassSupport
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
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
@ActiveProfiles(Array("carrot.processing.peakdetection", "carrot.lcms", "file.source.eclipse", "test", "teddy","carrot.targets.yaml.annotation","carrot.targets.yaml.correction"))
class LCMSRetentionIndexCorrectionMassErrorTest extends WordSpec with Matchers with Logging {
  val libName = "teddy"

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val deco: PeakDetection = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "LCMSRetentionIndexCorrectionTest" should {
    val name = "B2a_SA0973_TEDDYLipids_Neg_1GZSZ.mzml"

    s"should pass, because we have enough standards for us to continue ${name}" in {
      val sample = loader.getSample(name)
      val method = AcquisitionMethod(ChromatographicMethod(libName, Some("6550"), Some("test"), Some(NegativeMode())))
      val corrected = correction.process(deco.process(sample, method, None), method, None)

      corrected.featuresUsedForCorrection.foreach { x =>
        logger.info(s"used for correction: ${x.target.name}")
        val feature = new AccurateMassSupport {
          override def accurateMass: Option[Double] = Some(x.annotation.massOfDetectedFeature.get.mass)
        }
        val massError = MassAccuracy.calculateMassError(feature, x.target).get
        val massErrorPPM = MassAccuracy.calculateMassErrorPPM(feature, x.target).get
        logger.info(s"\tmass error: ${massError}")
        logger.info(s"\tmass error PPM: ${massErrorPPM}")
        massError should be <= correction.massAccuracySetting
        massErrorPPM should be <= correction.massAccuracyPPMSetting
      }

      assert(corrected.regressionCurve != null)
    }

  }
}
