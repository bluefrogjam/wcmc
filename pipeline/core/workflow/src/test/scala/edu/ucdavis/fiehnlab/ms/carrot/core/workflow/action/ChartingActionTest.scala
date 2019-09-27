package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{NegativeMode, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.ZeroReplacementProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import org.apache.logging.log4j.scala.Logging
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@SpringBootTest
@ActiveProfiles(
  Array("carrot.report.quantify.height",
    "carrot.processing.peakdetection",
    "carrot.processing.replacement.mzrt",
    "file.source.eclipse",
    "carrot.lcms",
    "test",
    "carrot.targets.yaml.annotation", "carrot.targets.yaml.correction",
    "charting"))
class ChartingActionTest extends WordSpec with Matchers with Logging {
  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val deconv: PeakDetection = null

  @Autowired
  val correction: CorrectionProcess = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

  @Autowired
  val quantification: QuantifyByHeightProcess = null

  @Autowired
  val chartingAction: ChartingAction2[Double] = null

  @Autowired
  val replacementProperties: ZeroReplacementProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ChartingAction" should {
    //    annotation.lcmsProperties.retentionIndexWindow = 5
    //    replacementProperties.retentionIndexWindowForPeakDetection = 15

    "create EIC charts negative mode" in {

      val sample = loader.getSample("B2a_TEDDYLipids_Neg_QC006.mzml")
      val method = AcquisitionMethod(ChromatographicMethod("teddy", Some("6550"), Some("test"), Some(NegativeMode())))

      val expClass = ExperimentClass(Seq(sample), None)
      val experiment = Experiment(Seq(expClass), None, method)
      val qsample = quantification.process(
        annotation.process(
          correction.process(
            deconv.process(sample, method, None),
            method, None),
          method, None),
        method, Some(sample))

      chartingAction.run(qsample, expClass, experiment)
    }

    "create EIC charts positive mode" in {

      val sample = loader.getSample("B2a_TEDDYLipids_Pos_QC006.mzml")
      val method = AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Some(PositiveMode())))

      val expClass = ExperimentClass(Seq(sample), None)
      val experiment = Experiment(Seq(expClass), None, method)
      val qsample = quantification.process(
        annotation.process(
          correction.process(
            deconv.process(sample, method, None),
            method, None),
          method, None),
        method, None)

      chartingAction.run(qsample, expClass, experiment)
    }
  }
}
