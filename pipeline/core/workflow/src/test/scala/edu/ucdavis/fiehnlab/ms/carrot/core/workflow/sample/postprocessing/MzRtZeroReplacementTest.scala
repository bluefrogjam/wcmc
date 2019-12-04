package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import java.io.File

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{GapFilledTarget, NegativeMode, QuantifiedSample}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("test",
  "carrot.lcms",
  "file.source.eclipse",
  "teddy",
  "carrot.report.quantify.height",
  "carrot.processing.replacement.mzrt",
  "carrot.processing.peakdetection",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation"))
class MzRtZeroReplacementTest extends WordSpec with Logging with Matchers with BeforeAndAfterAll {
  val libName = "teddy"

  @Autowired
  val zeroReplacement: MzRtZeroReplacement = null

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val deco: PeakDetection = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

  @Autowired
  val quantify: QuantifyByHeightProcess = null

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val stasis_cli: StasisService = null

  override def beforeAll() = {
    new File(s"${sys.env.get("java.io.temp")}/B2a_TEDDYLipids_Neg_QC006.mzml").delete()
  }

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "MzRtZeroReplacementTest" should {
    val method = AcquisitionMethod(ChromatographicMethod(libName, Some("6550"), Some("test"), Some(NegativeMode())))

    "replace the null values in the file" in {
      lazy val rawSample = loader.getSample("B2a_TEDDYLipids_Neg_QC006.mzml")
      lazy val sample: QuantifiedSample[Double] = quantify.process(
        annotation.process(
          correction.process(
            deco.process(
              rawSample, method, None),
            method, None),
          method, None),
        method, Some(rawSample))

      zeroReplacement.zeroReplacementProperties.estimateByNearestFourIons = false
      val replaced: QuantifiedSample[Double] = zeroReplacement.process(sample, method, Some(rawSample))

      logger.info("name, rts, ri, mz, int")
      replaced.quantifiedTargets.foreach { x =>
        if (x.quantifiedValue.isEmpty)
          logger.info(f"${x.name.get}, ${x.retentionTimeInSeconds}%.2f, ${x.retentionIndex}%.2f, ${x.accurateMass.get}%.4f, ${x.quantifiedValue.getOrElse(0.0)}%.0f")
        else
          x.quantifiedValue.getOrElse(-1.0) should be >= 0.0
      }

      //all spectra should be the same count as the targets
      replaced.spectra.size should be(replaced.quantifiedTargets.size)

      //should have GapFilledTargets
      replaced.quantifiedTargets.collect {
        case s: GapFilledTarget[Double] => s
      } should not be empty
    }

    "replace the null values in the file using nearest ion estimation" in {
      lazy val rawSample = loader.getSample("B2a_TEDDYLipids_Neg_QC006.mzml")
      lazy val sample: QuantifiedSample[Double] = quantify.process(
        annotation.process(
          correction.process(
            deco.process(
              rawSample, method, None),
            method, None),
          method, None),
        method, Some(rawSample))

      zeroReplacement.zeroReplacementProperties.estimateByNearestFourIons = true
      val replaced: QuantifiedSample[Double] = zeroReplacement.process(sample, method, Some(rawSample))

      logger.info("name, rts, ri, mz, int")
      // All target must be nonzero
      replaced.quantifiedTargets.foreach { x =>
        if (x.quantifiedValue.isEmpty)
          logger.info(f"target: ${x.name.get}, ${x.retentionTimeInSeconds}%.2f, ${x.retentionIndex}%.2f, ${x.accurateMass.get}%.4f, ${x.quantifiedValue.getOrElse(0.0)}%.0f")
        else
          x.quantifiedValue.getOrElse(-1.0) should be >= 0.0
      }

      //all spectra should be the same count as the targets
      replaced.spectra.size should be(replaced.quantifiedTargets.size)

      //should have GapFilledTargets
      replaced.quantifiedTargets.collect {
        case s: GapFilledTarget[Double] => s
      } should not be empty
    }
  }
}
