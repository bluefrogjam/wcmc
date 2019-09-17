package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSMSSpectra
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

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("carrot.report.quantify.height",
  "carrot.processing.peakdetection", "carrot.lcms", "file.source.eclipse",
  "test", "teddy","carrot.targets.yaml.annotation","carrot.targets.yaml.correction"))
class MSMSWorkflowTest extends WordSpec with Logging with Matchers {
  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

  @Autowired
  val quantification: QuantifyByHeightProcess = null

  @Autowired
  val deco: PeakDetection = null

  @Autowired
  val stasis_cli: StasisService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "The process" should {
    "return some negative mode MSMS spectra" in {
      val sample: Sample = loader.getSample("B2b_SA1594_TEDDYLipids_Neg_MSMS_1U2WN.mzml")
      val method = AcquisitionMethod(ChromatographicMethod("teddy", Some("6550"), Some("test"), Option(NegativeMode())))

      val neg_result = quantification.process(
        annotation.process(
          correction.process(
            deco.process(sample, method, None),
            method, Some(sample)),
          method, None),
        method, Some(sample))

      val msms = neg_result.spectra.count(_.isInstanceOf[MSMSSpectra])
      logger.info(s"# of   annotated MSMS: $msms")
      logger.info(s"# of unannotated MSMS: ${neg_result.noneAnnotated.count(_.isInstanceOf[MSMSSpectra])}")
      msms should be > 0
    }

    "return some positive mode MSMS spectra" in {
      val sample = loader.getSample("B1A_SA0001_TEDDYLipids_Pos_1RAR7_MSMS.mzml")
      val method = AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Option(PositiveMode())))

      val pos_result = quantification.process(
        annotation.process(
          correction.process(
            deco.process(sample, method, None),
            method, Some(sample)),
          method, None),
        method, Some(sample))

      val msms = pos_result.spectra.count(_.isInstanceOf[MSMSSpectra])
      logger.info(s"# of   annotated MSMS: $msms")
      logger.info(s"# of unannotated MSMS: ${pos_result.noneAnnotated.count(_.isInstanceOf[MSMSSpectra])}")
      msms should be > 0
    }
  }
}
