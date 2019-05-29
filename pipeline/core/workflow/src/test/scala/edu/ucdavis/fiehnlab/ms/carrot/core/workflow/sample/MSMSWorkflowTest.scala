package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSMSSpectra
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action.AddToLibraryAction
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
  "carrot.processing.peakdetection", "carrot.lcms", "file.source.luna",
  "carrot.processing.replacement.mzrt",
  "carrot.targets.dynamic",
  "test", "teddy"))
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

  @Autowired
  val action: AddToLibraryAction = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "The process" should {

    "return some negative mode MSMS spectra" in {
      val sample = loader.loadSample("B2b_SA1594_TEDDYLipids_Neg_MSMS_1U2WN.mzml")
      val method = AcquisitionMethod(ChromatographicMethod("teddy", Some("6550"), Some("test"), Option(NegativeMode())))
      val expClass = ExperimentClass(Seq(sample.get), None)
      val experiment = Experiment(Seq(expClass), Some("test MSMS bin generation"), method)

      val result = quantification.process(
        annotation.process(
          correction.process(
            deco.process(sample.get, method, None),
            method, sample),
          method, None),
        method, sample)

      val msms = result.spectra.collect {
        case spec: MSMSSpectra => spec
      }
      val nonAnnotated = result.noneAnnotated.collect {
        case spec: MSMSSpectra => spec
      }

      logger.info(s"# of   annotated MSMS: ${msms.size}")
      logger.info(s"# of unannotated MSMS: ${nonAnnotated.size}")

      msms.size should be > 0
      nonAnnotated.size should be > 0

      action.run(result, expClass, experiment)
    }

    "return some positive mode MSMS spectra" in {
      val sample = loader.loadSample("B1_SA0001_TEDDYLipids_Pos_1RAR7_MSMS.mzml")
      val method = AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Option(PositiveMode())))
      val expClass = ExperimentClass(Seq(sample.get), None)
      val experiment = Experiment(Seq(expClass), Some("test MSMS bin generation"), method)

      val result = quantification.process(
        annotation.process(
          correction.process(
            deco.process(sample.get, method, None),
            method, sample),
          method, None),
        method, sample)

      val msms = result.spectra.collect {
        case spec: MSMSSpectra => spec
      }
      val nonAnnotated = result.noneAnnotated.collect {
        case spec: MSMSSpectra => spec
      }

      logger.info(s"# of   annotated MSMS: ${msms.size}")
      logger.info(s"# of unannotated MSMS: ${nonAnnotated.size}")

      msms.size should be > 0
      nonAnnotated.size should be > 0

      action.run(result, expClass, experiment)
    }
  }
}
