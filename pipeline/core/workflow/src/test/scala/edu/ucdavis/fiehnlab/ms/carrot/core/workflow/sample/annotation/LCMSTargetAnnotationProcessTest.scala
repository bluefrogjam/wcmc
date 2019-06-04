package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{PositiveMode, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
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
@ActiveProfiles(Array("file.source.luna", "carrot.report.quantify.height", "carrot.processing.peakdetection", "carrot.lcms", "test", "carrot.targets.yaml.annotation", "carrot.targets.yaml.correction"))
class LCMSTargetAnnotationProcessTest extends WordSpec with Logging {
@ActiveProfiles(Array("file.source.luna", "carrot.report.quantify.height", "carrot.processing.peakdetection", "carrot.lcms", "test"))
class LCMSTargetAnnotationProcessTest extends WordSpec with Matchers with Logging {

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

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

  val samples: Seq[_ <: Sample] = loader.getSamples(Seq("B5_P20Lipids_Pos_NIST01.mzml", "B5_P20Lipids_Pos_NIST02.mzml"))

  "LCMSTargetAnnotationProcessTest" should {

    "ensure it's variables are defined" in {
      assert(annotation.targets != null)
    }

    //compute purity values
    val purityComputed = samples //.map(purity.process)

    val method = AcquisitionMethod(ChromatographicMethod("lcms_istds", Some("test"), Some("test"), Some(PositiveMode())))

    val targetValues = Map("B5_P20Lipids_Pos_NIST01" -> Map(
      "1_CUDA iSTD [M+H]+_HPTJABJPZMULFH-UHFFFAOYSA-N" -> 47.7910,
      "1_Sphingosine(d17:1) iSTD [M+H]+_RBEJCQPPFCKTRZ-LHMZYYNSSA-N" -> 62.764999,
      "1_LPE(17:1) iSTD [M+H]+_LNJNONCNASQZOB-HEDKFQSOSA-N" -> 82.2330,
      "1_LPC(17:0) iSTD [M+H]+_SRRQPVVYXBTRQK-XMMPIXPASA-N" -> 111.682998,
      "1_MG(17:0/0:0/0:0) iSTD [M+Na]+_SVUQHVRAGMNPLW-UHFFFAOYSA-N" -> 183.561996,
      "1_DG(18:1/2:0/0:0) iSTD [M+Na]+_PWTCCMJTPHCGMS-YRBAHSOBSA-N" -> 191.5480,
      "1_DG(18:1/2:0/0:0) iSTD [M+NH4]+_PWTCCMJTPHCGMS-YRBAHSOBSA-N" -> 191.5480,
      "1_PC(12:0/13:0) iSTD [M+H]+_FCTBVSCBBWKZML-WJOKGBTCSA-N" -> 211.514999,
      "1_DG(12:0/12:0/0:0) iSTD [M+Na]+_OQQOAWVKVDAJOI-VWLOTQADSA-N" -> 257.43698,
      "1_Cholesterol d7 iSTD [M-H2O+H]+_HVYWMOMLDIMFJA-IFAPJKRJSA-N" -> 291.3800,
      "1_SM(d18:1/17:0) iSTD [M+H]+_YMQZQHIESOAPQH-JXGHDCMNSA-N" -> 306.359985,
      "1_Cer(d18:1/17:0) iSTD [M+H-H2O]+_ICWGMOFDULMCFL-QKSCFGQVSA-N" -> 361.266998,
      "1_Cer(d18:1/17:0) iSTD [M+Na]+_ICWGMOFDULMCFL-QKSCFGQVSA-N" -> 361.76599,
      "1_PE(17:0/17:0) iSTD [M+H]+_YSFFAUPDXKTJMR-DIPNUNPCSA-N" -> 380.2350,
      "1_TAG d5(17:0/17:1/17:0) iSTD [M+Na]+_OWYYELCHNALRQZ-ADIIQMQPSA-N" -> 666.25299,
      "1_CE(22:1) iSTD [M+NH4]+_SQHUGNAFKZZXOT-JWTURFAQSA-N" -> 709.18097
    ),
      "B5_P20Lipids_Pos_NIST02" -> Map(
        "1_CUDA iSTD [M+H]+_HPTJABJPZMULFH-UHFFFAOYSA-N" -> 47.6669998,
        "1_Sphingosine(d17:1) iSTD [M+H]+_RBEJCQPPFCKTRZ-LHMZYYNSSA-N" -> 63.639999,
        "1_LPE(17:1) iSTD [M+H]+_LNJNONCNASQZOB-HEDKFQSOSA-N" -> 82.1090,
        "1_LPC(17:0) iSTD [M+H]+_SRRQPVVYXBTRQK-XMMPIXPASA-N" -> 111.559998,
        "1_MG(17:0/0:0/0:0) iSTD [M+Na]+_SVUQHVRAGMNPLW-UHFFFAOYSA-N" -> 183.93899,
        "1_DG(18:1/2:0/0:0) iSTD [M+Na]+_PWTCCMJTPHCGMS-YRBAHSOBSA-N" -> 190.9270,
        "1_DG(18:1/2:0/0:0) iSTD [M+NH4]+_PWTCCMJTPHCGMS-YRBAHSOBSA-N" -> 191.42599,
        "1_PC(12:0/13:0) iSTD [M+H]+_FCTBVSCBBWKZML-WJOKGBTCSA-N" -> 211.3930,
        "1_DG(12:0/12:0/0:0) iSTD [M+Na]+_OQQOAWVKVDAJOI-VWLOTQADSA-N" -> 257.3150,
        "1_Cholesterol d7 iSTD [M-H2O+H]+_HVYWMOMLDIMFJA-IFAPJKRJSA-N" -> 290.7590,
        "1_SM(d18:1/17:0) iSTD [M+H]+_YMQZQHIESOAPQH-JXGHDCMNSA-N" -> 305.7340,
        "1_Cer(d18:1/17:0) iSTD [M+H]+_ICWGMOFDULMCFL-QKSCFGQVSA-N" -> 360.6410,
        "1_Cer(d18:1/17:0) iSTD [M+Na]+_ICWGMOFDULMCFL-QKSCFGQVSA-N" -> 361.6390,
        "1_PE(17:0/17:0) iSTD [M+H]+_YSFFAUPDXKTJMR-DIPNUNPCSA-N" -> 380.1080,
        "1_TAG d5(17:0/17:1/17:0) iSTD [M+NH4]+_OWYYELCHNALRQZ-ADIIQMQPSA-N" -> 665.6270,
        "1_CE(22:1) iSTD [M+NH4]+_SQHUGNAFKZZXOT-JWTURFAQSA-N" -> 708.55499
      ))

    // deconvolute and correct the data
    val correctedSamples = purityComputed.map(item => correction.process(deco.process(item, method, None), method, Some(item)))

    correctedSamples.foreach { sample =>
      s"process ${sample} without recursive annotation and using gaussian similarity" in {

        annotation.lcmsProperties.recursiveAnnotationMode = false

        val result = annotation.process(sample, method, None)

        //        result.spectra.foreach { spectra => //sortBy(_.target.name.get).
        //          logger.debug(f"${spectra.target.name.get}")
        //          logger.debug(f"\ttarget data:")
        //          logger.debug(f"\t\t mass:           ${spectra.target.precursorMass.get}%1.4f")
        //          logger.debug(f"\t\t rt (s):         ${spectra.target.retentionIndex}%1.3f")
        //          logger.debug(f"\tannotation data:")
        //          logger.debug(f"\t\t scan:           ${spectra.scanNumber}")
        //          logger.debug(f"\t\t mass:           ${spectra.accurateMass.get}%1.4f")
        //          logger.debug(f"\t\t rt (s):         ${spectra.retentionTimeInSeconds}%1.3f")
        //          logger.debug(f"\t\t mass accuracy:  ${spectra.massAccuracy.get}%1.5f")
        //          logger.debug(f"\t\t mass accuracy:  ${spectra.massAccuracyPPM.get}%1.3f} ppm")
        //          logger.debug(f"\t\t distance ri:    ${spectra.retentionIndexDistance.get}%1.3f")
        //          logger.debug("")
        //        }

        assert(result != null)
        assert(result.noneAnnotated.size != result.spectra.size)
        assert((result.noneAnnotated.size + result.spectra.size) == result.correctedWith.spectra.size)

        val quantified = quantify.process(result, method, Some(sample))

        logger.debug(s"quantified: ${quantified.quantifiedTargets.size}")

        //these are our ISTD being compared to the quantified features
        targetValues(sample.name).foreach(tgt => {
          logger.info(s"${tgt._1} => ${tgt._2}")
          val q = quantified.quantifiedTargets.filter(_.name.get == tgt._1)
          logger.info(s"filtered: ${q.size}")
          q.head.retentionIndex shouldBe tgt._2 +- annotation.lcmsProperties.retentionIndexWindow * 2
        })
      }

      s"process ${sample} with recursive annotation and using gaussian similarity" in {

        annotation.lcmsProperties.recursiveAnnotationMode = true

        val result = annotation.process(sample, method, None)

        //        logger.debug(s"sample name: ${sample.fileName}")
        //        result.spectra.sortBy(_.target.name.get).foreach { spectra =>
        //          logger.debug(f"${spectra.target.name.get}")
        //          logger.debug(f"\ttarget data:")
        //          logger.debug(f"\t\t mass:          ${spectra.target.precursorMass.get}%1.4f")
        //          logger.debug(f"\t\t rt (s):        ${spectra.target.retentionIndex}%1.3f")
        //          logger.debug(f"\tannotation data:")
        //          logger.debug(f"\t\t scan:          ${spectra.scanNumber}")
        //          logger.debug(f"\t\t mass:          ${spectra.accurateMass.get}%1.4f")
        //          logger.debug(f"\t\t ri (s):        ${spectra.retentionIndex}%1.3f")
        //          logger.debug(f"\t\t rt (s):        ${spectra.retentionTimeInSeconds}%1.3f")
        //          logger.debug(f"\t\t mass accuracy: ${spectra.massAccuracy.get}%1.5f")
        //          logger.debug(f"\t\t mass accuracy: ${spectra.massAccuracyPPM.get}%1.3f ppm")
        //          logger.debug(f"\t\t distance ri:   ${spectra.retentionIndexDistance.get}%1.3f")
        //          logger.debug("")
        //        }

        assert(result != null)
        assert(result.noneAnnotated.size != result.spectra.size)
        assert((result.noneAnnotated.size + result.spectra.size) == result.correctedWith.spectra.size)

        val quantified = quantify.process(result, method, Some(sample))

        //these are our ISTD being compared to the annotated features
        targetValues(sample.name).foreach(tgt => {
          logger.info(s"target: ${
            quantified.quantifiedTargets.filter(_.name.get == tgt._1)
                .map(f => s"${f.retentionTimeInSeconds} (${f.precursorMass}:${f.quantifiedValue})").mkString("; ")
          }")
          quantified.quantifiedTargets.filter(_.name.get == tgt._1).head.retentionIndex shouldBe tgt._2 +- annotation.lcmsProperties.retentionIndexWindow * 2
        })

      }
    }
  }
}
