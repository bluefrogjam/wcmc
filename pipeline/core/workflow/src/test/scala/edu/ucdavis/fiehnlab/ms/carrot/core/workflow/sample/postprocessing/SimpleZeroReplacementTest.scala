package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.PostProcessing
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.QuantifiedSample
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlg on 7/13/2016.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("backend-txt-lcms", "carrot.report.quantify.height", "carrot.processing.replacement.simple", "carrot.processing.peakdetection", "carrot.lcms", "file.source.luna", "file.source.eclipse", "file.source.localhost"))
class SimpleZeroReplacementTest extends WordSpec with LazyLogging with ShouldMatchers {

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
  val postprocess: PostProcessing[Double] = null

  @Autowired
  val loader: SampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "SimpleZeroReplacementTest" must {
    val method = AcquisitionMethod(None)

    val sample: QuantifiedSample[Double] =
      quantify.process(annotation.process(
        correction.process(
          deco.process(
            loader.getSample("B5_P20Lipids_Pos_QC000.d.zip"), method
          ), method
        ), method
      ), method)

    "replaceValue" should {

      var replaced: QuantifiedSample[Double] = null
      "replace the null values in the file" in {
        replaced = simpleZeroReplacement.process(sample, method)


        replaced.spectra.foreach { x =>
          logger.info(s"spectra: ${x}")
        }

        logger.info("---")
        replaced.quantifiedTargets.foreach { x =>
          logger.info(s"target: ${x}")
        }

        //all spectra should be the same count as the targets
        replaced.spectra.size should be(replaced.quantifiedTargets.size)
      }
    }

  }
}
