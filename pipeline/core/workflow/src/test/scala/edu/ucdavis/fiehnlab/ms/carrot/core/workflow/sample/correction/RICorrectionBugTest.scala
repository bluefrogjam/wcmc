package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, SampleLoader, TxtStreamLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by diego on 4/12/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("file.source.luna","quantify-by-scan", "carrot.processing.peakdetection", "carrot.lcms", "carrot.logging.json.enable"))
class RICorrectionBugTest extends WordSpec with ShouldMatchers with LazyLogging {

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val deco: PeakDetection = null

  @Autowired
  val libraryAccess: LibraryAccess[Target] = null

  @Autowired
  val loader: SampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Retention Index Correction Process" should {
    val method = AcquisitionMethod(ChromatographicMethod("targets_20180315", None, None, Some(PositiveMode())))
    val sample: CorrectedSample = correction.process(
      deco.process(
        loader.getSample("Weiss005_posHILIC_40298234_039.mzML"), method, None
      ), method, None
    )

    "find the closest feature for each target" in {
      sample.featuresUsedForCorrection.foreach(x => x.annotation.retentionTimeInSeconds === x.target.retentionIndex +- 10)
    }

    "check that TG[M+NH4]+ comes at the same RT that [M+Na]+" in {
      val tgAdducts = sample.featuresUsedForCorrection.filter(x => x.target.name.get.startsWith("1_TG"))
      tgAdducts shouldBe 2
      tgAdducts.head.annotation.retentionTimeInMinutes shouldBe tgAdducts
    }

    "choose the correct TG peak" in {
      // Weiss003_posHILIC_59602960_068.mzML
      // 1_TG d5(17:0/17:1/17:0) iSTD [M+Na]+_OWYYELCHNALRQZ-ADIIQMQPSA-N
      val wrongFeature = new Feature {

        override val ionMode: Option[IonMode] = None
        override val purity: Option[Double] = None
        override val sample: String = null
        override val retentionTimeInSeconds: Double = 634.632019042969
        override val scanNumber: Int = -1
        override val associatedScan: Option[SpectrumProperties] = None
        override val massOfDetectedFeature: Option[Ion] = Some(Ion(874.79089510745, 100))
        /**
          * the signal noise of this spectra
          */
        override val signalNoise: Option[Double] = None
        /**
          * the unique mass of this spectra
          */
        override val uniqueMass: Option[Double] = None
      }

      val correctFeature = new Feature {
        override val ionMode: Option[IonMode] = None
        override val purity: Option[Double] = None
        override val sample: String = null
        override val retentionTimeInSeconds: Double = 666.015014648438
        override val scanNumber: Int = -1
        override val associatedScan: Option[SpectrumProperties] = None
        override val massOfDetectedFeature: Option[Ion] = Some(Ion(874.792541148294, 100))
        /**
          * the signal noise of this spectra
          */
        override val signalNoise: Option[Double] = None
        /**
          * the unique mass of this spectra
          */
        override val uniqueMass: Option[Double] = None

      }

      val target = new Target {
        override val retentionIndex: Double = 659.622
        override var confirmed: Boolean = false
        override val precursorMass: Option[Double] = Some(874.7882)
        override var inchiKey: Option[String] = None
        override var name: Option[String] = None
        override var requiredForCorrection: Boolean = false
        override var isRetentionIndexStandard: Boolean = true
        override val spectrum: Option[SpectrumProperties] = None
        /**
          * the unique mass of this spectra
          */
        override val uniqueMass: Option[Double] = None

      }

      correction.gaussianSimilarity(wrongFeature, target) should be < 0.5
      correction.gaussianSimilarity(correctFeature, target) should be > 0.5
      correction.findBestHit(target, Seq(wrongFeature, correctFeature)).annotation shouldBe correctFeature
    }
  }
}

@Configuration
class CorrectionTestConfig {
  @Autowired
  val resourceLoader: ResourceLoader = null

  @Bean
  def libraryAccess: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](resourceLoader.loadAsFile("targets_20180315.txt").get, "\t")

}
