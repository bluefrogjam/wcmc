package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.storage

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{SampleToProcess, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.ZeroReplacement
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
  "carrot.output.storage.local",
  "carrot.resource.store.local",
  "carrot.output.writer.txt"
))
class LocalResultStorageTest extends WordSpec {
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
  val localResultStorage: LocalResultStorage = null

  new TestContextManager(this.getClass).prepareTestInstance(this)
  "LocalResultStorageTest" should {


    val method = AcquisitionMethod(ChromatographicMethod(libName, Some("test"), Some("test"), Some(PositiveMode())))

    val samples: Seq[_ <: Sample] = sampleLoader.getSamples(Seq("B5_P20Lipids_Pos_QC000.mzml", "B5_P20Lipids_Pos_NIST02.mzml"))

    val deconvoluted = samples.map((item: Sample) => deconv.process(item, method, Some(item)))

    //correct the data
    val correctedSample = deconvoluted.map((item: Sample) => correction.process(item, method, Some(item)))

    val annotated = correctedSample.map((item: CorrectedSample) => annotation.process(item, method, Some(item)))

    val quantified = annotated.map((item: AnnotatedSample) => quantification.process(item, method, Some(item)))

    val results = quantified.map((item: QuantifiedSample[Double]) => replacement.process(item, method, Some(item)))
    "store" in {

      localResultStorage.store(Experiment(
        name = Some("test"),
        acquisitionMethod = method,

        classes = Seq(ExperimentClass(quantified, None)

        )), task = Task(name = "test", email = None, acquisitionMethod = method, samples = samples.map(x => SampleToProcess(fileName = x.fileName, className = "test"))))
    }

  }
}
