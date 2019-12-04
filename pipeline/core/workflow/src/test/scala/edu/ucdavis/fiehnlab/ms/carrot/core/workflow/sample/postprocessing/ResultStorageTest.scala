package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{SampleToProcess, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod, Matrix}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("test",
  "carrot.lcms",
  "file.source.eclipse",
  "carrot.lcms.correction",
  "carrot.report.quantify.height",
  "carrot.processing.peakdetection",
  "carrot.processing.replacement.simple",
  "carrot.targets.yaml.annotation",
  "carrot.targets.yaml.correction",
  "carrot.output.writer.json",
  "carrot.output.storage.converter.sample",
  "carrot.output.storage.converter.target",
  "carrot.runner.required"
))
class ResultStorageTest extends WordSpec {
  val libName = "lcms_istds"

  @Autowired
  val deconv: PeakDetection = null

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

  @Autowired
  val quantification: QuantifyByHeightProcess = null

  @Autowired
  val replacement: ZeroReplacement = null

  @Autowired
  val sampleLoader: SampleLoader = null

  @Autowired
  val resultStorage: ResultStorage = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ResultStorageTest" should {

    val method = AcquisitionMethod(ChromatographicMethod(libName, Some("test"), Some("test"), Some(PositiveMode())))

    "store without msms" in {
      lazy val samples: Seq[_ <: Sample] = sampleLoader.getSamples(Seq("B5_P20Lipids_Pos_QC000.mzml", "B5_P20Lipids_Pos_NIST02.mzml"))

      lazy val quantified = samples.map((item: Sample) => quantification.process(
        annotation.process(
          correction.process(
            deconv.process(item, method, None),
            method, None),
          method, None),
        method, Some(item)))

      resultStorage.store(Experiment(
        name = Some("test"),
        acquisitionMethod = method,
        classes = Seq(
          ExperimentClass(quantified, Some(Matrix("matrix1", "human", "plasma", Seq.empty)))
        )),
        task = Task(name = "test", email = None, acquisitionMethod = method, samples = samples.map(x => SampleToProcess(fileName = x.fileName, className = "test"))))

    }

    "store with msms" in {
      lazy val sample: Sample = sampleLoader.getSample("B1A_SA0001_TEDDYLipids_Pos_1RAR7_MSMS.mzml")

      lazy val quantified = quantification.process(
        annotation.process(
          correction.process(
            deconv.process(sample, method, None),
            method, None),
          method, None),
        method, Some(sample))

      resultStorage.store(Experiment(
        name = Some("test"),
        acquisitionMethod = method,
        classes = Seq(ExperimentClass(Seq(quantified), None)
        )),
        task = Task(name = "test", email = None, acquisitionMethod = method,
          samples = Seq(SampleToProcess(fileName = sample.fileName, className = "test"))))
    }

  }
}

