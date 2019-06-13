package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.QuantificationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{PositiveMode, QuantifiedSample}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
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
@ActiveProfiles(Array("carrot.report.quantify.height",
  "carrot.processing.replacement.mzrt",
  "carrot.processing.peakdetection",
  "carrot.lcms", "file.source.luna",
  "test",
  "carrot.targets.yaml.annotation",
  "carrot.targets.yaml.correction"))
class NegativeIntensitiesTest extends WordSpec with Logging with Matchers {
  val libName = "lcms_istds"

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val deco: PeakDetection = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

  @Autowired
  val quantify: QuantificationProcess[Double] = null

  @Autowired
  val loader: SampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ZeroReplacement" should {
    val method = AcquisitionMethod(ChromatographicMethod(libName, Some("test"), Some("test"), Some(PositiveMode())))
    val rawSample = loader.getSample("B5_P20Lipids_Pos_QC000.mzml")

    val sample: QuantifiedSample[Double] = quantify.process(
      annotation.process(
        correction.process(
          deco.process(rawSample,
            method, None),
          method, None),
        method, None),
      method, Some(rawSample))

    "replace the with 0 intensitiy or leave positive values" in {

      sample.quantifiedTargets.foreach { x =>
        logger.info(s"target: ${x.name.get} = ${x.quantifiedValue}")
        x.quantifiedValue.getOrElse(-1.0) should be >= 0.0
      }

      //all spectra should be the same count as the targets
      sample.spectra.size should be(sample.quantifiedTargets.size)
    }
  }

}
