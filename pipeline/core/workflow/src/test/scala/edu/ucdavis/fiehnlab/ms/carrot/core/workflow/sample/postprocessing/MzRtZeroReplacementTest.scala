package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

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
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("carrot.report.quantify.height",
  "carrot.processing.replacement.mzrt",
  "carrot.processing.peakdetection",
  "carrot.lcms",
  "file.source.luna",
  "teddy",
  "test"))
class MzRtZeroReplacementTest extends WordSpec with Logging with Matchers {
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

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "MzRtZeroReplacementTest" must {
    val method = AcquisitionMethod(ChromatographicMethod(libName, Some("6550"), Some("test"), Some(NegativeMode())))
    val rawSample = loader.getSample("B2a_TEDDYLipids_Neg_QC006.mzml")
    val sample: QuantifiedSample[Double] = quantify.process(
      annotation.process(
        correction.process(
          deco.process(
            rawSample, method, None),
          method, Some(rawSample)),
        method, Some(rawSample)),
      method, Some(rawSample))

    "replaceValue" should {

      "replace the null values in the file" in {
        zeroReplacement.zeroReplacementProperties.estimateByNearestFourIons = false
        val replaced: QuantifiedSample[Double] = zeroReplacement.process(sample, method, Some(rawSample))

        replaced.quantifiedTargets.foreach { x =>
          logger.info(s"target: ${x.accurateMass.get % .4f}_${x.name.get} = ${x.quantifiedValue.getOrElse("none")}")
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
        zeroReplacement.zeroReplacementProperties.estimateByNearestFourIons = true
        val replaced: QuantifiedSample[Double] = zeroReplacement.process(sample, method, Some(rawSample))

        // All target must be nonzero
        replaced.quantifiedTargets.foreach { x =>
          logger.info(s"target: ${x.accurateMass.get % .4f}_${x.name.get} = ${x.quantifiedValue.getOrElse("none")}")
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
}
