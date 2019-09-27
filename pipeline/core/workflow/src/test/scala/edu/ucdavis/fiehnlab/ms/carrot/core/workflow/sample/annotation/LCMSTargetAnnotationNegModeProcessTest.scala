package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.NegativeMode
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlgemuth on 6/27/16.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("file.source.eclipse",
  "carrot.report.quantify.height",
  "carrot.processing.peakdetection",
  "carrot.processing.replacement.mzrt",
  "carrot.lcms",
  "test", "carrot.targets.yaml.annotation", "carrot.targets.yaml.correction"))
class LCMSTargetAnnotationNegModeProcessTest extends WordSpec with Matchers with Logging {

  @Autowired
  val correction: CorrectionProcess = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

  @Autowired
  val lcmsProperties: LCMSAnnotationProcessProperties = null

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val deco: PeakDetection = null

  @Autowired
  val stasis_cli: StasisService = null

  @Autowired
  val quantify: QuantifyByHeightProcess = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "LCMSTargetAnnotationNegModeProcessTest" should {

    "ensure it's variables are defined" in {
      assert(annotation.targets != null)
    }

    val sample = loader.getSample("B2a_TEDDYLipids_Neg_QC006.mzml")
    val method = AcquisitionMethod(ChromatographicMethod("teddy", Some("6550"), Some("test"), Some(NegativeMode())))

    val targetValues = Map[String, Double](
      "1_Ceramide (d18:1/17:0) iSTD [M+Cl]-_ICWGMOFDULMCFL-QKSCFGQVSA-N" -> 363,
      "1_Ceramide (d18:1/17:0) iSTD [M+FA-H]-_ICWGMOFDULMCFL-QKSCFGQVSA-N" -> 363,
      "1_CUDA iSTD [M-H]-_HPTJABJPZMULFH-UHFFFAOYSA-N" -> 45,
      "1_FA iSTD (16:0)-d3 [M-H]-_IPCSVZSSVZVIGE-FIBGUPNXSA-N" -> 189,
      "1_LPC (17:0) iSTD [M+FA-H]-_SRRQPVVYXBTRQK-XMMPIXPASA-N" -> 108.6,
      "1_LPE (17:1) iSTD [M-H]-_LNJNONCNASQZOB-HEDKFQSOSA-N" -> 77.4,
      "1_MAG (17:0/0:0/0:0) iSTD [M+FA-H]-_SVUQHVRAGMNPLW-UHFFFAOYSA-N" -> 183.6,
      "1_PC (12:0/13:0) iSTD [M+FA-H]-_FCTBVSCBBWKZML-WJOKGBTCSA-N" -> 214.2,
      "1_PE (17:0/17:0) iSTD [M-H]-_YSFFAUPDXKTJMR-DIPNUNPCSA-N" -> 380.4,
      "1_PG (17:0/17:0) iSTD [M-H]-_ZBVHXVKEMAIWQQ-QPPIDDCLSA-N" -> 336.60,
      "1_SM (d18:1/17:0) iSTD [M+FA-H]-_YMQZQHIESOAPQH-JXGHDCMNSA-N" -> 309.6,
      "Ceramide (d42:2) B [M+FA-H]-_VJSBNBBOSZJDKB-KPEYJIHVSA-N" -> 471.6
    )

    //correct the data
    val correctedSample = correction.process(deco.process(sample, method, None), method, Some(sample))

    s"process ${sample} without recursive annotation and using gaussian similarity" in {

      annotation.lcmsProperties.recursiveAnnotationMode = false
      annotation.lcmsProperties.preferGaussianSimilarityForAnnotation = true

      val result = annotation.process(correctedSample, method, Some(sample))

      result.spectra.foreach { spectra => //sortBy(_.target.name.get).
        logger.debug(f"${spectra.target.name.get}")
        logger.debug(f"\ttarget data:")
        logger.debug(f"\t\t mass:           ${spectra.target.precursorMass.get}%1.4f")
        logger.debug(f"\t\t rt (s):         ${spectra.target.retentionIndex}%1.3f")
        logger.debug(f"\tannotation data:")
        logger.debug(f"\t\t scan:           ${spectra.scanNumber}")
        logger.debug(f"\t\t mass:           ${spectra.accurateMass.get}%1.4f")
        logger.debug(f"\t\t rt (s):         ${spectra.retentionTimeInSeconds}%1.3f")
        logger.debug(f"\t\t mass accuracy:  ${spectra.massAccuracy.get}%1.5f")
        logger.debug(f"\t\t mass accuracy:  ${spectra.massAccuracyPPM.get}%1.3f} ppm")
        logger.debug(f"\t\t distance ri:    ${spectra.retentionIndexDistance.get}%1.3f")
        logger.debug("")
      }

      assert(result != null)
      assert(result.noneAnnotated.size != result.spectra.size)
      assert((result.noneAnnotated.size + result.spectra.size) == result.correctedWith.spectra.size)

      val quantified = quantify.process(result, method, None)

      logger.debug(s"quantified: ${quantified.quantifiedTargets.size}")

      //these are our ISTD being compared to the annotated features
      targetValues.foreach(tgt => {
        logger.info(s"target: ${
          quantified.quantifiedTargets.filter(_.name.get == tgt._1)
              .map(f => s"${f.retentionIndex} (${f.precursorMass.get})").mkString("; ")
        }")
        quantified.quantifiedTargets.filter(_.name.get == tgt._1).head.retentionIndex shouldBe tgt._2 +- annotation.lcmsProperties.retentionIndexWindow
      })
    }
  }
}
