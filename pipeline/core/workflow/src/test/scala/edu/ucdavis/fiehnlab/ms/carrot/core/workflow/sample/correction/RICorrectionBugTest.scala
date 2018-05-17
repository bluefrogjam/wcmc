package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, SampleLoader}
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
@ActiveProfiles(Array("file.source.luna", "carrot.processing.peakdetection", "carrot.lcms"))
class RICorrectionBugTest extends WordSpec with ShouldMatchers with LazyLogging {

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val deco: PeakDetection = null

  @Autowired
  val libraryAccess: LibraryAccess[Target] = null

  @Autowired
  val loader: SampleLoader = null


  def buildFeature(mz: Double, rt: Double, intensity: Float = 100): Feature = new Feature {
    override val ionMode: Option[IonMode] = None
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
    override var confirmed: Boolean = false
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
      val method = AcquisitionMethod(ChromatographicMethod("targets", None, None, Some(PositiveMode())))
      val sample: CorrectedSample = correction.process(
        deco.process(
          loader.getSample("Weiss005_posHILIC_40298234_039.mzML"), method, None
        ), method, None
      )

      "find the closest feature for each target" in {
        sample.featuresUsedForCorrection.foreach(x => x.annotation.retentionTimeInSeconds === x.target.retentionIndex +- 10)
      }
    }

    "choose the correct 1_TG d5(17:0/17:1/17:0) M+Na peak in Weiss003_posHILIC_59602960_068.mzML" in {
      // 1_TG d5(17:0/17:1/17:0) iSTD [M+Na]+_OWYYELCHNALRQZ-ADIIQMQPSA-N
      val wrongFeature = buildFeature(874.79089510745, 634.632019042969, 1000000)
      val correctFeature = buildFeature(874.792541148294, 666.015014648438, 10000)
      val target = buildTarget(874.7882, 659.622)

      gaussianSimilarity(wrongFeature, target) should be < 0.54
      gaussianSimilarity(correctFeature, target) should be > 0.88
      correction.findBestHit(target, Seq(wrongFeature, correctFeature)).annotation shouldBe correctFeature
    }

    "choose the correct 1_TG d5(17:0/17:1/17:0) M+NH4 peak in Weiss005_posHILIC_40298234_039.mzML" in {
      // 1_TG d5(17:0/17:1/17:0) iSTD [M+Na]+_OWYYELCHNALRQZ-ADIIQMQPSA-N
      val veryWrongFeature = buildFeature(869.842010008935, 653.929016113281, 8900)
      val wrongFeature = buildFeature(869.825428194475, 658.910034179688, 8300)
      val correctFeature = buildFeature(869.831824427935, 666.880004882812, 25000)
      val target = buildTarget(869.8329, 659.622)

      gaussianSimilarity(veryWrongFeature, target) should be < 0.7
      gaussianSimilarity(wrongFeature, target) should be < 0.75
      gaussianSimilarity(correctFeature, target) should be > 0.9
      correction.findBestHit(target, Seq(wrongFeature, correctFeature)).annotation shouldBe correctFeature
    }
  }
}
