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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlg on 7/13/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("carrot.report.quantify.height", "carrot.processing.replacement.simple", "carrot.processing.peakdetection", "carrot.lcms", "file.source.luna", "test"))
class SimpleZeroReplacementTest extends WordSpec with Logging with Matchers {
  val libName = "lcms_istds"

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
    val method = AcquisitionMethod(ChromatographicMethod(libName, Some("test"), Some("test"), Some(PositiveMode())))
    val rawSample = loader.getSample("B5_P20Lipids_Pos_QC000.mzml")
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
        simpleZeroReplacement.zeroReplacementProperties.estimateByNearestFourIons = false
        val replaced: QuantifiedSample[Double] = simpleZeroReplacement.process(sample, method, Some(rawSample))

        replaced.quantifiedTargets.foreach { x =>
          logger.info(s"target: ${x.name.get} = ${x.quantifiedValue}")
          x.quantifiedValue.getOrElse(-1.0) should be >= 0.0
        }

        //all spectra should be the same count as the targets
        replaced.spectra.size should be(replaced.quantifiedTargets.size)

        //should have GapFilledTargets
        replaced.spectra.collect {
          case s: GapFilledTarget[Double] => s
        } should not be empty

        // tracking status should be updated
        stasis_cli.getTracking(sample.name).status.map(_.value) should contain("replaced")
      }

      "replace the null values in the file using nearest ion estimation" in {
        simpleZeroReplacement.zeroReplacementProperties.estimateByNearestFourIons = true
        val replaced: QuantifiedSample[Double] = simpleZeroReplacement.process(sample, method, Some(rawSample))

        // All target must be nonzero
        replaced.quantifiedTargets.foreach { x =>
          logger.info(s"target: ${x.name.get} = ${x.quantifiedValue}")
          x.quantifiedValue.getOrElse(-1.0) should be > 0.0
        }

        //all spectra should be the same count as the targets
        replaced.spectra.size should be(replaced.quantifiedTargets.size)

        //should have GapFilledTargets
        replaced.spectra.collect {
          case s: GapFilledTarget[Double] => s
        } should not be empty

        // tracking status should be updated
        stasis_cli.getTracking(sample.name).status.map(_.value) should contain("replaced")
      }
    }
  }
}
