package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.math.MassAccuracy
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.{AccurateMassSupport, Feature}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotatedSpectra, NegativeMode, QuantifiedTarget, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.SimpleZeroReplacement
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
@ActiveProfiles(Array("file.source.luna", "carrot.processing.replacement.simple", "carrot.report.quantify.height", "carrot.processing.peakdetection", "carrot.lcms", "test", "teddy"))
class LCMSTargetAnnotationMassErrorTest extends WordSpec with Matchers with Logging {
  val libName = "teddy"

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

  @Autowired
  val simpleZeroReplacement: SimpleZeroReplacement = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "LCMSTargetAnnotationMassErrorTest" should {

    "ensure it's variables are defined" in {
      assert(annotation.targets != null)
    }

    val targetValues = Map[String, Map[String, Double]](
      "1_MAG (17:0/0:0/0:0) iSTD [M+FA-H]-_SVUQHVRAGMNPLW-UHFFFAOYSA-N" -> Map[String, Double]("mz" -> 389.29085, "int" -> 10, "rt" -> 183.6),
      "1_PC (12:0/13:0) iSTD [M+FA-H]-_FCTBVSCBBWKZML-WJOKGBTCSA-N" -> Map[String, Double]("mz" -> 680.45185, "int" -> 10, "rt" -> 214.2),
      "FA (10:0) (capric acid) [M-H]-_GHVNFZFCNZKVNT-UHFFFAOYSA-N" -> Map[String, Double]("mz" -> 171.1384, "int" -> 10, "rt" -> 40.8),
      "FA (18:2) (linoleic acid) [M-H]-_JBYXPOFIGCOSSB-XBLVEGMJSA-N" -> Map[String, Double]("mz" -> 279.2332, "int" -> 10, "rt" -> 116.4),
      "FAHFA (26:0); FAHFA (16:0/O-10:0) [M-H]-_LHZYTJUUOMBIQG-UHFFFAOYNA-N" -> Map[String, Double]("mz" -> 425.3636, "int" -> 10, "rt" -> 225.6),
      "FAHFA (36:2); FAHFA (18:0/O-18:2) [M-H]-" -> Map[String, Double]("mz" -> 561.4888, "int" -> 10, "rt" -> 349.8),
      "FAHFA (36:3); FAHFA (18:1/O-18:2) [M-H]-_BXKZSDVPBFDTEK-CCSKTMJNNA-N" -> Map[String, Double]("mz" -> 559.4732, "int" -> 10, "rt" -> 318.0),
      "FAHFA (32:2); FAHFA (14:1/O-18:1) [M-H]-_SISJHZPVSPJWAA-SDUNWBGTNA-N" -> Map[String, Double]("mz" -> 505.4262, "int" -> 10, "rt" -> 271.2))

    val name = "B2a_SA0973_TEDDYLipids_Neg_1GZSZ.mzml"

    s"process $name without recursive annotation and using gaussian similarity" in {
      val sample = loader.loadSample("B2a_SA0973_TEDDYLipids_Neg_1GZSZ.mzml")
      val method = AcquisitionMethod(ChromatographicMethod("teddy", Some("6550"), Some("test"), Some(NegativeMode())))
      //correct the data
      val correctedSample = correction.process(deco.process(sample.get, method, None), method, sample)


      annotation.lcmsProperties.recursiveAnnotationMode = false
      annotation.lcmsProperties.preferGaussianSimilarityForAnnotation = true

      val result = annotation.process(correctedSample, method, None)
      logger.info(s"annotated: ${result.spectra.size}")

      assert(result != null)

      //      result.spectra.filterNot(a => a.target.name.get == "Unknown")
      //          .map(a => AnnotationReport(a, a.target))
      //          .sortBy(_.featureMassErrorPPM)
      //          .foreach(a => logger.info(a))

      logger.info("----------")

      val quant = quantify.process(result, method, None)
      logger.info(s"quantified: ${quant.quantifiedTargets.count(_.quantifiedValue.isDefined)}")
      logger.info(s"quantified: ${quant.spectra.size}")

      //      quant.quantifiedTargets.filter(_.quantifiedValue.isDefined)
      //          .filterNot(q => q.spectra.get.target.name.getOrElse("Unknown") == "Unknown")
      //          .map(a => AnnotationReport(a.spectra.get, a.spectra.get.target))
      //          .sortBy(_.featureMassErrorPPM)
      //          .foreach(q => logger.info(q))

      logger.info("----------")

      val replaced = simpleZeroReplacement.process(quant, method, sample)
      logger.info(s"quantified: ${replaced.quantifiedTargets.count(_.quantifiedValue.isDefined)}")
      logger.info(s"quantified: ${replaced.spectra.size}")

      val replQuant: Seq[QuantifiedTarget[Double]] = replaced.quantifiedTargets.filter(_.quantifiedValue.isDefined)
          .filterNot(q => q.spectra.get.target.name.getOrElse("Unknown") == "Unknown")

      //      replQuant.map(a => AnnotationReport(a.spectra.get, a.spectra.get.target))
      //          .foreach(q => {
      //            logger.info(q)
      //          })
      targetValues.foreach(t => {
        replQuant.filter(_.spectra.get.target.name == t._1)
            .foreach(q => {
              val expectedMass: Double = t._2.filter(_._1 == "mz").head._2
              val expectedRT: Double = t._2.filter(_._1 == "rt").head._2
              q.accurateMass shouldBe expectedMass +- annotation.lcmsProperties.massAccuracySettingPpm
              q.accurateMass shouldBe expectedRT +- annotation.lcmsProperties.retentionIndexWindow
            })
      })
    }
  }
}

case class AnnotationReport(
                               feature: Feature with AnnotatedSpectra,
                               target: Target
                           ) {
  def calculatedErrorPPM: Double = {
    MassAccuracy.calculateMassErrorPPM(
      new AccurateMassSupport {
        override def accurateMass: Option[Double] = Some(featureMass)
      }, target).getOrElse(Int.MaxValue.asInstanceOf[Double])
  }

  def featureMass: Double = feature.accurateMass.get

  def targetMass: Double = target.accurateMass.get

  def featureMassErrorPPM: Double = feature.massAccuracyPPM.getOrElse(calculatedErrorPPM)

  override def toString: String = {
    f"targetName: ${target.name.getOrElse("Unknown")}%-25s, featureMass: ${featureMass}%1.4f, targetMass: ${target.accurateMass.getOrElse(0.0)}%1.4f, " +
        f"featureMassErrorPPM: ${featureMassErrorPPM}%1.4f, calculated massErrorPPM: ${calculatedErrorPPM}%1.4f"
  }
}
