package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, SampleLoader}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
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
@ActiveProfiles(Array("file.source.luna", "quantify-by-scan", "carrot.processing.peakdetection", "carrot.lcms" , "test"/*, "carrot.logging.json.enable"*/))
class Weiss2RICorrectionBugTest extends WordSpec with ShouldMatchers with LazyLogging {

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val deco: PeakDetection = null

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val libraryAccess: LibraryAccess[Target] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "RI Correction on weiss002" should {

    "choose the correct ammoniated TG peak at 666.88s (11.11m)" in {
      val wrongFeature1 = new Feature {
        override val ionMode: Option[IonMode] = None
        override val purity: Option[Double] = None
        override val sample: String = null
        override val retentionTimeInSeconds: Double = 337.6080017089844
        override val scanNumber: Int = -1
        override val associatedScan: Option[SpectrumProperties] = None
        override val massOfDetectedFeature: Option[Ion] = Some(Ion(869.8283293189685, 100))
        override val signalNoise: Option[Double] = None
        override val uniqueMass: Option[Double] = None
      }
      val wrongFeature2 = new Feature {
        override val ionMode: Option[IonMode] = None
        override val purity: Option[Double] = None
        override val sample: String = null
        override val retentionTimeInSeconds: Double = 307.7190246582031
        override val scanNumber: Int = -1
        override val associatedScan: Option[SpectrumProperties] = None
        override val massOfDetectedFeature: Option[Ion] = Some(Ion(869.8284506827555, 100))
        override val signalNoise: Option[Double] = None
        override val uniqueMass: Option[Double] = None
      }
      val wrongFeature3 = new Feature {
        override val ionMode: Option[IonMode] = None
        override val purity: Option[Double] = None
        override val sample: String = null
        override val retentionTimeInSeconds: Double = 653.9290161132812
        override val scanNumber: Int = -1
        override val associatedScan: Option[SpectrumProperties] = None
        override val massOfDetectedFeature: Option[Ion] = Some(Ion(869.8420100089353, 100))
        override val signalNoise: Option[Double] = None
        override val uniqueMass: Option[Double] = None
      }
      val wrongFeature4 = new Feature { //most dificult
        override val ionMode: Option[IonMode] = None
        override val purity: Option[Double] = None
        override val sample: String = null
        override val retentionTimeInSeconds: Double = 658.9100341796875
        override val scanNumber: Int = -1
        override val associatedScan: Option[SpectrumProperties] = None
        override val massOfDetectedFeature: Option[Ion] = Some(Ion(869.8254281944745, 100))
        override val signalNoise: Option[Double] = None
        override val uniqueMass: Option[Double] = None
      }

      val correctFeature = new Feature {
        override val ionMode: Option[IonMode] = None
        override val purity: Option[Double] = None
        override val sample: String = null
        override val retentionTimeInSeconds: Double = 666.8800048828125
        override val scanNumber: Int = -1
        override val associatedScan: Option[SpectrumProperties] = None
        override val massOfDetectedFeature: Option[Ion] = Some(Ion(869.8318244279354, 100))
        override val signalNoise: Option[Double] = None
        override val uniqueMass: Option[Double] = None

      }

      val target = new Target {
        override val retentionIndex: Double = 659.6220000000001
        override var confirmed: Boolean = false
        override val precursorMass: Option[Double] = Some(869.8329)
        override var inchiKey: Option[String] = None
        override var name: Option[String] = None
        override var requiredForCorrection: Boolean = false
        override var isRetentionIndexStandard: Boolean = true
        override val spectrum: Option[SpectrumProperties] = None
        override val uniqueMass: Option[Double] = None

      }

      correction.findBestHit(target, Seq(wrongFeature1, wrongFeature2, wrongFeature3, wrongFeature4, correctFeature)).annotation shouldBe correctFeature
    }

    "choose the correct sodiated TG peak" in {
      val wrongFeature1 = new Feature {
        override val ionMode: Option[IonMode] = None
        override val purity: Option[Double] = None
        override val sample: String = null
        override val retentionTimeInSeconds: Double = 612.0850219726562
        override val scanNumber: Int = -1
        override val associatedScan: Option[SpectrumProperties] = None
        override val massOfDetectedFeature: Option[Ion] = Some(Ion(874.7783727260261, 100))
        override val signalNoise: Option[Double] = None
        override val uniqueMass: Option[Double] = None
      }
      val wrongFeature2 = new Feature {
        override val ionMode: Option[IonMode] = None
        override val purity: Option[Double] = None
        override val sample: String = null
        override val retentionTimeInSeconds: Double = 653.9290161132812
        override val scanNumber: Int = -1
        override val associatedScan: Option[SpectrumProperties] = None
        override val massOfDetectedFeature: Option[Ion] = Some(Ion(874.7933894961609, 100))
        override val signalNoise: Option[Double] = None
        override val uniqueMass: Option[Double] = None
      }

      val correctFeature = new Feature {
        override val ionMode: Option[IonMode] = None
        override val purity: Option[Double] = None
        override val sample: String = null
        override val retentionTimeInSeconds: Double = 667.3779907226562
        override val scanNumber: Int = -1
        override val associatedScan: Option[SpectrumProperties] = None
        override val massOfDetectedFeature: Option[Ion] = Some(Ion(874.789048438798, 100))
        override val signalNoise: Option[Double] = None
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
        override val uniqueMass: Option[Double] = None
      }

      correction.findBestHit(target, Seq(wrongFeature1, wrongFeature2, correctFeature)).annotation shouldBe correctFeature
    }
  }
}
