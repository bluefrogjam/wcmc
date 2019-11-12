package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{GapFilledTarget, PositiveMode, QuantifiedSample}
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

/**
  * Created by wohlg on 7/13/2016.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array(
  "test",
  "carrot.lcms",
  "file.source.eclipse",
  "carrot.report.quantify.height",
  "carrot.processing.replacement.simple",
  "carrot.processing.peakdetection",
  "carrot.targets.yaml.annotation",
  "carrot.targets.yaml.correction"))
class SimpleZeroReplacementTest extends WordSpec with Logging with Matchers {
  @Autowired
  val simpleZeroReplacement: SimpleZeroReplacement = null

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

  "SimpleZeroReplacementTest" must {
    val libName = "teddy"
    val instrument = Some("6530")
    val method = AcquisitionMethod(ChromatographicMethod(libName, instrument, Some("test"), Some(PositiveMode())))
    val rawSample = loader.getSample("B5_P20Lipids_Pos_QC029.mzml")
    val sample: QuantifiedSample[Double] = quantify.process(
      annotation.process(
        correction.process(
          deco.process(
            rawSample, method, None),
          method, None),
        method, None),
      method, Some(rawSample))

    "replaceValue" should {

      "replace the null values in the file" in {
        simpleZeroReplacement.zeroReplacementProperties.estimateByNearestFourIons = false
        val replaced: QuantifiedSample[Double] = simpleZeroReplacement.process(sample, method, Some(rawSample))

        replaced.quantifiedTargets.foreach { x =>
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
        simpleZeroReplacement.zeroReplacementProperties.estimateByNearestFourIons = true
        val replaced: QuantifiedSample[Double] = simpleZeroReplacement.process(sample, method, Some(rawSample))

        // All target must be nonzero
        replaced.quantifiedTargets.foreach { x =>
          x.quantifiedValue.getOrElse(-1.0) should be >= 0.0
        }

        //should have GapFilledTargets
        replaced.quantifiedTargets.collect {
          case s: GapFilledTarget[Double] => s
        } should not be empty

      }
    }
  }
}
