package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.action

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod, Matrix}
import edu.ucdavis.fiehnlab.ms.carrot.core.db.mona.MonaLibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.{ResultStorage, ZeroReplacement}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.scalatest.time.SpanSugar._
import org.scalatest.{Matchers, WordSpec}
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
  "carrot.filters.ioncount",
  "carrot.filters.intensity",
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
class AddToLibraryActionTest extends WordSpec with Matchers with Logging with Eventually {
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
  val action: AddToLibraryAction = null

  @Autowired
  val mona: MonaLibraryAccess = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "AddToLibraryAction" should {

    val method = AcquisitionMethod(ChromatographicMethod("csh", Some("6550"), Some("test"), Some(NegativeMode())))

    "have more than 1 target filter" in {
      action.targetFilters should have length 2
    }

    "add unknowns to mona" in {
      val sample: Sample = sampleLoader.getSample("lgvty_cells_pilot_2_NEG_500K_01.mzml")

      val quantified = quantification.process(
        annotation.process(
          correction.process(
            deconv.process(sample, method, None),
            method, None),
          method, None),
        method, Some(sample))

      eventually(timeout(value = 10 seconds), interval(value = 1 second)) {
        mona.deleteLibrary(method, Some(false))
        mona.load(method, Some(false)) should have size 0
      }

      val classes = Seq(ExperimentClass(Seq(quantified), Some(Matrix("matrix1", "human", "plasma", Seq.empty))))
      val experiment = Experiment(classes, Some("test"), method)

      action.run(quantified, classes.head, experiment)

      eventually(timeout(value = 5 seconds), interval(value = 1 second)) {
        mona.load(experiment.acquisitionMethod, Some(false)) should have size 145
      }

      action.run(quantified, classes.head, experiment)
      Thread.sleep(2000)

      mona.load(experiment.acquisitionMethod, Some(false)) should have size 145
    }
  }
}

