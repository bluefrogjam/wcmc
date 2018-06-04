package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{PositiveMode, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByScanProcess
import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlgemuth on 6/27/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("file.source.luna", "quantify-by-scan", "carrot.processing.peakdetection", "carrot.lcms", "carrot.lcms.correction" /*, "carrot.logging.json.enable"*/))
class LCMSTargetAnnotationProcessTest extends WordSpec with LazyLogging {

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

  @Autowired
  val lcmsProperties: LCMSAnnotationProperties = null

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val deco: PeakDetection = null

  /**
    * used to verify picked scans are correct
    */
  @Autowired
  val quantify: QuantifyByScanProcess = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "LCMSTargetAnnotationProcessTest" should {

    "ensure it's variables are defined" in {
      assert(annotation.targets != null)
    }

    val samples: Seq[_ <: Sample] = loader.getSamples(Seq("B5_P20Lipids_Pos_NIST01.mzml", "B5_P20Lipids_Pos_NIST02.mzml"))

    //compute purity values
    val purityComputed = samples //.map(purity.process)

    val method = AcquisitionMethod(ChromatographicMethod("lcms_istds", Some("test"), Some("test"), Some(PositiveMode())))

    val targetValues = Map("B5_P20Lipids_Pos_NIST01" -> Map(
      "1_CUDA iSTD [M+H]+_HPTJABJPZMULFH-UHFFFAOYSA-N" -> 47.579002380371094,
      "1_Sphingosine(d17:1) iSTD [M+H]+_RBEJCQPPFCKTRZ-LHMZYYNSSA-N" -> 62.55000305175782,
      "1_LPE(17:1) iSTD [M+H]+_LNJNONCNASQZOB-HEDKFQSOSA-N" -> 82.01200103759766,
      "1_LPC(17:0) iSTD [M+H]+_SRRQPVVYXBTRQK-XMMPIXPASA-N" -> 111.45600128173828,
      "1_MG(17:0/0:0/0:0) iSTD [M+Na]+_SVUQHVRAGMNPLW-UHFFFAOYSA-N" -> 183.31700134277344,
      "1_DG(18:1/2:0/0:0) iSTD [M+Na]+_PWTCCMJTPHCGMS-YRBAHSOBSA-N" -> 190.80299377441406,
      "1_PC(12:0/13:0) iSTD [M+H]+_FCTBVSCBBWKZML-WJOKGBTCSA-N" -> 211.26400756835938,
      "1_DG(12:0/12:0/0:0) iSTD [M+Na]+_OQQOAWVKVDAJOI-VWLOTQADSA-N" -> 257.1759948730469,
      "1_Cholesterol d7 iSTD [M–H2O+H]+_HVYWMOMLDIMFJA-IFAPJKRJSA-N" -> 291.1099853515625,
      "1_SM(d18:1/17:0) iSTD [M+H]+_YMQZQHIESOAPQH-JXGHDCMNSA-N" -> 306.08099365234375,
      "1_Cer(d18:1/17:0) iSTD [M+Na]+_ICWGMOFDULMCFL-QKSCFGQVSA-N" -> 361.4750061035156,
      "1_PE(17:0/17:0) iSTD [M+H]+_YSFFAUPDXKTJMR-DIPNUNPCSA-N" -> 380.43798828125,
      "1_TG d5(17:0/17:1/17:0) iSTD [M+Na]+_OWYYELCHNALRQZ-ADIIQMQPSA-N" -> 666.3880004882812,
      "1_CE(22:1) iSTD [2M+NH4]+_SQHUGNAFKZZXOT-JWTURFAQSA-N" -> 708.8060302734375
    ),
      "B5_P20Lipids_Pos_NIST02" -> Map(
        "1_CUDA iSTD [M+H]+_HPTJABJPZMULFH-UHFFFAOYSA-N" -> 46.71000289916992,
        "1_Sphingosine(d17:1) iSTD [M+H]+_RBEJCQPPFCKTRZ-LHMZYYNSSA-N" -> 63.34199523925781,
        "1_LPE(17:1) iSTD [M+H]+_LNJNONCNASQZOB-HEDKFQSOSA-N" -> 82.10900115966797,
        "1_LPC(17:0) iSTD [M+H]+_SRRQPVVYXBTRQK-XMMPIXPASA-N" -> 111.15599822998047,
        "1_MG(17:0/0:0/0:0) iSTD [M+Na]+_SVUQHVRAGMNPLW-UHFFFAOYSA-N" -> 182.3159942626953,
        "1_DG(18:1/2:0/0:0) iSTD [M+Na]+_PWTCCMJTPHCGMS-YRBAHSOBSA-N" -> 190.42201232910156,
        "1_PC(12:0/13:0) iSTD [M+H]+_FCTBVSCBBWKZML-WJOKGBTCSA-N" -> 211.1639862060547,
        "1_DG(12:0/12:0/0:0) iSTD [M+Na]+_OQQOAWVKVDAJOI-VWLOTQADSA-N" -> 255.70201110839844,
        "1_Cholesterol d7 iSTD [M–H2O+H]+_HVYWMOMLDIMFJA-IFAPJKRJSA-N" -> 288.5639953613281,
        "1_SM(d18:1/17:0) iSTD [M+H]+_YMQZQHIESOAPQH-JXGHDCMNSA-N" -> 303.822021484375,
        "1_Cer(d18:1/17:0) iSTD [M+Na]+_ICWGMOFDULMCFL-QKSCFGQVSA-N" -> 361.6390075683594,
        "1_PE(17:0/17:0) iSTD [M+H]+_YSFFAUPDXKTJMR-DIPNUNPCSA-N" -> 376.10400390625,
        "1_TG d5(17:0/17:1/17:0) iSTD [M+Na]+_OWYYELCHNALRQZ-ADIIQMQPSA-N" -> 661.3200073242188,
        "1_CE(22:1) iSTD [2M+NH4]+_SQHUGNAFKZZXOT-JWTURFAQSA-N" -> 702.9960327148438
      ))

    //correct the data
    val correctedSample = purityComputed.map((item: Sample) => correction.process(deco.process(item, method), method))

    correctedSample.foreach { sample =>
      s"process ${sample} without recursive annotation and with preferring mass accuracy over retention index distance" in {

        annotation.lcmsProperties.recursiveAnnotationMode = false
        annotation.lcmsProperties.preferGaussianSimilarityForAnnotation = true

        val result = annotation.process(sample, method, None)

        assert(result != null)
        assert(result.noneAnnotated.size != result.spectra.size)
        assert((result.noneAnnotated.size + result.spectra.size) == result.correctedWith.spectra.size)

        result.featuresUsedForCorrection.foreach { spectra => //sortBy(_.target.name.get).
          logger.debug(f"${spectra.target.name.get}")
          logger.debug(f"\ttarget data:")
          logger.debug(f"\t\t mass:          ${spectra.target.precursorMass.get}%1.4f")
          logger.debug(f"\t\t rt (s):        ${spectra.target.retentionIndex}%1.3f")
          logger.debug(f"\t\t rt (m):        ${spectra.target.retentionTimeInMinutes}%1.3f")
          logger.debug(f"\tannotation data:")
          logger.debug(f"\t\t ri (m):        ${spectra.annotation.retentionTimeInMinutes}%1.3f")
          logger.debug(f"\t\t ri (s):        ${spectra.annotation.retentionTimeInSeconds}%1.3f")
          logger.debug(f"\t\t rt (m):        ${spectra.annotation.retentionTimeInMinutes}%1.3f")
          logger.debug(f"\t\t rt (s):        ${spectra.annotation.retentionTimeInSeconds}%1.3f")
          logger.debug(f"\t\t mass accuracy: --") //${spectra.massAccuracy.get}%1.5f
          logger.debug(f"\t\t mass accuracy: --") //${/*spectra.massAccuracyPPM.get*/}%1.3f ppm
          logger.debug(f"\t\t distance ri:   --") //${/*spectra.retentionIndexDistance.get*/}%1.3f

          logger.debug("")
        }
        val quantified = quantify.process(result, method, None)

        logger.debug(s"quantified: ${quantified.quantifiedTargets.size}")
        //these are our ISD
        targetValues(sample.name).foreach(tgt =>
          quantified.spectra.filter(_.target.name.get == tgt._1).head.retentionTimeInSeconds shouldBe tgt._2 +- 0.02
        )
      }

      s"process ${sample} with recursive annotation and with preferring mass accuracy over retention index distance" in {

        annotation.lcmsProperties.recursiveAnnotationMode = true
        annotation.lcmsProperties.preferGaussianSimilarityForAnnotation = true

        val result = annotation.process(sample, method, None)

        assert(result != null)
        assert(result.noneAnnotated.size != result.spectra.size)
        assert((result.noneAnnotated.size + result.spectra.size) == result.correctedWith.spectra.size)

        logger.debug(s"sample name: ${sample.fileName}")
        result.spectra.sortBy(_.target.name.get).foreach { spectra =>
          logger.debug(f"${spectra.target.name.get}")
          logger.debug(f"\ttarget data:")
          logger.debug(f"\t\t mass:          ${spectra.target.precursorMass.get}%1.4f")
          logger.debug(f"\t\t rt (s):        ${spectra.target.retentionIndex}%1.3f")
          logger.debug(f"\t\t rt (m):        ${spectra.target.retentionTimeInMinutes}%1.3f")
          logger.debug(f"\tannotation data:")
          logger.debug(f"\t\t ri (m):        ${spectra.retentionIndex / 60}%1.3f")
          logger.debug(f"\t\t ri (s):        ${spectra.retentionIndex}%1.3f")
          logger.debug(f"\t\t rt (s):        ${spectra.retentionTimeInSeconds}%1.3f")
          logger.debug(f"\t\t rt (m):        ${spectra.retentionTimeInMinutes}%1.3f")
          logger.debug(f"\t\t mass accuracy: ${spectra.massAccuracy.get}%1.5f")
          logger.debug(f"\t\t mass accuracy: ${spectra.massAccuracyPPM.get}%1.3f ppm")
          logger.debug(f"\t\t distance ri:   ${spectra.retentionIndexDistance.get}%1.3f")


          logger.debug("")
        }
        val quantified = quantify.process(result, method)

        //these are our ISD
        targetValues(sample.name).foreach(tgt =>
          quantified.spectra.filter(_.target.name.get == tgt._1).head.retentionTimeInSeconds shouldBe tgt._2 +- 0.02
        )
      }
    }

  }
}
