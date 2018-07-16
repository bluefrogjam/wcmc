package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.math.SimilarityMethods
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by diego on 4/12/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("file.source.luna","file.source.eclipse", "carrot.processing.peakdetection", "carrot.lcms", "test"))
class RICorrectionBugTest extends WordSpec with ShouldMatchers with LazyLogging {

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val deco: PeakDetection = null

  @Autowired
  val loader: SampleLoader = null


  def buildFeature(mz: Double, rt: Double, intensity: Float = 100): Feature = new Feature {
    override val ionMode: Option[IonMode] = Some(PositiveMode())
    override val purity: Option[Double] = None
    override val sample: String = null
    override val retentionTimeInSeconds: Double = rt
    override val scanNumber: Int = -1
    override val associatedScan: Option[SpectrumProperties] = None
    override val massOfDetectedFeature: Option[Ion] = Some(Ion(mz, intensity))
    override val signalNoise: Option[Double] = None
    override val uniqueMass: Option[Double] = None
  }

  def buildTarget(mz: Double, rt: Double): Target = new Target {
    override val retentionIndex: Double = rt
    override var confirmed: Boolean = true
    override val precursorMass: Option[Double] = Some(mz)
    override var inchiKey: Option[String] = None
    override var name: Option[String] = None
    override var requiredForCorrection: Boolean = false
    override var isRetentionIndexStandard: Boolean = true
    override val spectrum: Option[SpectrumProperties] = None
    override val uniqueMass: Option[Double] = None
  }

  def gaussianSimilarity(feature: Feature, target: Target): Double = {
    SimilarityMethods.featureTargetSimilarity(feature, target, correction.massAccuracySetting, correction.rtAccuracySetting, correction.intensityPenaltyThreshold)
  }


  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Retention Index Correction Process" should {
    "test Weiss005_posHILIC_40298234_039.mzML" must {
      val method = AcquisitionMethod(ChromatographicMethod("lcms_istds", Some("test"), Some("test"), Some(PositiveMode())))
      val sample: CorrectedSample = correction.process(
        deco.process(
          loader.getSample("Weiss005_posHILIC_40298234_039.mzML"), method, None
        ), method, None
      )

      "find the closest feature for each target" in {
        sample.featuresUsedForCorrection.foreach(x => x.annotation.retentionTimeInSeconds === x.target.retentionIndex +- 10)
      }
    }

    "choose the correct 1_TG d5(17:0/17:1/17:0) M+Na peak in Weiss005_posHILIC_40298234_039.mzML" in {
      // 1_TG d5(17:0/17:1/17:0) iSTD [M+Na]+_OWYYELCHNALRQZ-ADIIQMQPSA-N
      val veryWrongFeature = buildFeature(874.7783727260261, 612.0850219726562, 100)
      val wrongFeature = buildFeature(874.7933894961609, 653.9290161132812, 100)
      val correctFeature = buildFeature(874.789048438798, 667.3779907226562, 25229)
      val target = buildTarget(874.7882, 659.622)

      val vw = gaussianSimilarity(veryWrongFeature, target)
      val w = gaussianSimilarity(wrongFeature, target)
      val c = gaussianSimilarity(correctFeature, target)

      logger.warn(s"very wrong:\t${vw}\nwrong:\t\t${w}\ncorrect:\t${c}")

      vw should be < 0.7
      w should be < 0.8
      c should be > 0.9
      correction.findBestHit(target, Seq(wrongFeature, correctFeature)).annotation shouldBe correctFeature
    }

    "choose the correct 1_TG d5(17:0/17:1/17:0) M+NH4 peak in Weiss005_posHILIC_40298234_039.mzML" in {
      // 1_TG d5(17:0/17:1/17:0) iSTD [M+Na]+_OWYYELCHNALRQZ-ADIIQMQPSA-N
      val veryWrongFeature = buildFeature(869.8420100089353, 653.9290161132812, 100)
      val wrongFeature = buildFeature(869.8254281944745, 658.9100341796875, 100)
      val correctFeature = buildFeature(869.8318244279354, 666.8800048828125, 25000)
      val target = buildTarget(869.8329, 659.622)

      val vw = gaussianSimilarity(veryWrongFeature, target)
      val w = gaussianSimilarity(wrongFeature, target)
      val c = gaussianSimilarity(correctFeature, target)

      logger.warn(s"very wrong:\t${vw}\nwrong:\t\t${w}\ncorrect:\t${c}")

      vw should be < 0.7
      w should be < 0.8
      c should be > 0.9
      correction.findBestHit(target, Seq(wrongFeature, correctFeature)).annotation shouldBe correctFeature
    }
  }
}
