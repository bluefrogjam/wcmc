package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{Feature, SpectrumProperties}
import edu.ucdavis.fiehnlab.ms.carrot.core.db.yaml.YAMLCorrectionLibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by diego on 4/12/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("file.source.eclipse", "carrot.report.quantify.height", "carrot.processing.peakdetection", "carrot.lcms", "test","carrot.targets.yaml.annotation","carrot.targets.yaml.correction"))
class Weiss5RICorrectionBugTest extends WordSpec with Matchers with Logging {

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val libraryAccess: YAMLCorrectionLibraryAccess = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "RI Correction on weiss005" should {
    logger.info(s"PPM ACCURACY: ${correction.massAccuracyPPMSetting}")
    logger.info(s"mDa ACCURACY: ${correction.massAccuracySetting}")

    "choose the correct ammoniated TG peak at 666.88s (11.11m)" in {
        val wrongFeature1 = new Feature {
            override val ionMode: Option[IonMode] = None
            override val purity: Option[Double] = None
            override val sample: String = null
          override val metadata: Map[String, AnyRef] = Map()
            override val retentionTimeInSeconds: Double = 337.6080017089844
            override val scanNumber: Int = -1
            override val associatedScan: Option[SpectrumProperties] = None
          override val massOfDetectedFeature: Option[Ion] = Some(Ion(869.8283293189685, 8150))
            override val signalNoise: Option[Double] = None
            override val uniqueMass: Option[Double] = None
        }
        val wrongFeature2 = new Feature {
            override val ionMode: Option[IonMode] = None
            override val purity: Option[Double] = None
            override val sample: String = null
            override val retentionTimeInSeconds: Double = 307.7190246582031
            override val scanNumber: Int = -1
          override val metadata: Map[String, AnyRef] = Map()
            override val associatedScan: Option[SpectrumProperties] = None
          override val massOfDetectedFeature: Option[Ion] = Some(Ion(869.8284506827555, 8500))
            override val signalNoise: Option[Double] = None
            override val uniqueMass: Option[Double] = None
        }
        val wrongFeature3 = new Feature {
            override val ionMode: Option[IonMode] = None
            override val purity: Option[Double] = None
            override val sample: String = null
            override val retentionTimeInSeconds: Double = 653.9290161132812
            override val scanNumber: Int = -1
          override val metadata: Map[String, AnyRef] = Map()
            override val associatedScan: Option[SpectrumProperties] = None
          override val massOfDetectedFeature: Option[Ion] = Some(Ion(869.8420100089353, 8750))
            override val signalNoise: Option[Double] = None
            override val uniqueMass: Option[Double] = None
        }
        val wrongFeature4 = new Feature { //most dificult
            override val ionMode: Option[IonMode] = None
            override val purity: Option[Double] = None
            override val sample: String = null
            override val retentionTimeInSeconds: Double = 658.9100341796875
          override val metadata: Map[String, AnyRef] = Map()
            override val scanNumber: Int = -1
            override val associatedScan: Option[SpectrumProperties] = None
          override val massOfDetectedFeature: Option[Ion] = Some(Ion(869.8254281944745, 8100))
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
          override val metadata: Map[String, AnyRef] = Map()
          override val massOfDetectedFeature: Option[Ion] = Some(Ion(869.8318244279354, 208450))
            override val signalNoise: Option[Double] = None
            override val uniqueMass: Option[Double] = None

        }

        val target = new Target {
          override val retentionIndex: Double = 659.622
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
        override val metadata: Map[String, AnyRef] = Map()
        override val sample: String = null
        override val retentionTimeInSeconds: Double = 612.09
        override val scanNumber: Int = 1
        override val associatedScan: Option[SpectrumProperties] = None
        override val massOfDetectedFeature: Option[Ion] = Some(Ion(874.7784, 924163))
        override val signalNoise: Option[Double] = None
        override val uniqueMass: Option[Double] = None
      }

      val wrongFeature2 = new Feature {
        override val ionMode: Option[IonMode] = None
        override val purity: Option[Double] = None
        override val sample: String = null
        override val retentionTimeInSeconds: Double = 653.93
        override val metadata: Map[String, AnyRef] = Map()
        override val scanNumber: Int = 2
        override val associatedScan: Option[SpectrumProperties] = None
        override val massOfDetectedFeature: Option[Ion] = Some(Ion(874.7934, 15134))
        override val signalNoise: Option[Double] = None
        override val uniqueMass: Option[Double] = None
      }

      val correctFeature = new Feature {
        override val ionMode: Option[IonMode] = None
        override val purity: Option[Double] = None
        override val sample: String = null
        override val metadata: Map[String, AnyRef] = Map()
        override val retentionTimeInSeconds: Double = 667.38
        override val scanNumber: Int = 10
        override val associatedScan: Option[SpectrumProperties] = None
        override val massOfDetectedFeature: Option[Ion] = Some(Ion(874.7890, 25640))
        override val signalNoise: Option[Double] = None
        override val uniqueMass: Option[Double] = None
      }

      val target = new Target {
        override val retentionIndex: Double = 661.2
        override var confirmed: Boolean = false
        override val precursorMass: Option[Double] = Some(874.7887)
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
