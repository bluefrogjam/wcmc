package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action

import java.util

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.action.PostAction
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod, Matrix}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.{ResultStorage, ZeroReplacement}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.collection.JavaConverters._

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
  "carrot.targets.dynamic",
  "carrot.targets.mona",
  "carrot.resource.store.local",
  "carrot.output.writer.json",
  "carrot.output.storage.converter.sample",
  "carrot.output.storage.converter.target",
  "carrot.runner.required"
))
class AddToLibraryActionTest extends WordSpec {
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

  @Autowired
  val actions: java.util.List[PostAction] = new util.ArrayList[PostAction]()

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "AddToLibraryAction" should {

    val method = AcquisitionMethod(ChromatographicMethod(libName, Some("test"), Some("test"), Some(NegativeMode())))

    "add unknowns to mona" in {
      val sample: Sample = sampleLoader.getSample("lgvty_cells_pilot_2_NEG_500K_01.mzml")

      val quantified = quantification.process(
        annotation.process(
          correction.process(
            deconv.process(sample, method, None),
            method, None),
          method, None),
        method, Some(sample))

      val classes = Seq(ExperimentClass(Seq(quantified), Some(Matrix("matrix1", "human", "plasma", Seq.empty))))
      val experiment = Experiment(classes, Some("test"), method)

      actions.asScala.collect {
        case add: AddToLibraryAction =>
          add.run(quantified, classes.head, experiment)
      }
    }

  }
}

