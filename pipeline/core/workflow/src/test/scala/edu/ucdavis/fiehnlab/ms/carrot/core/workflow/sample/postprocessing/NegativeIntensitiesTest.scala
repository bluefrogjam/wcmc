package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{PositiveMode, QuantifiedSample}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlg on 7/13/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("carrot.report.quantify.height", "carrot.processing.replacement.simple", "carrot.processing.peakdetection", "carrot.lcms", "file.source.luna", "file.source.eclipse"))
class NegativeIntensitiesTest extends WordSpec with LazyLogging with ShouldMatchers {

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

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ZeroReplacement" should {
    val method = AcquisitionMethod(ChromatographicMethod("lcms_istds", Some("test"), Some("test"), Some(PositiveMode())))
    val rawSample = loader.getSample("Weiss003_posHILIC_59602960_068.mzml")
    val sample: QuantifiedSample[Double] = quantify.process(
      annotation.process(
        correction.process(
          deco.process(rawSample,
            method, None),
          method, None),
        method, None),
      method, None)

    "replace the with 0 intensitiy or leave positive values" in {

      val replaced: QuantifiedSample[Double] = simpleZeroReplacement.process(sample, method, Some(rawSample))

      replaced.quantifiedTargets.foreach { x =>
        logger.info(s"target: ${x.name.get} = ${x.quantifiedValue}")
        x.quantifiedValue.getOrElse(-1.0) should be >= 0.0
      }

      //all spectra should be the same count as the targets
      replaced.spectra.size should be(replaced.quantifiedTargets.size)
    }
  }

}
