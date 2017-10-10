package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.acquisition.AcquisitionLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, SampleLoader}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSample, Target}
import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * This test is a case to ensure that our value is in the exspected range and this annotation
  * won't change based on configured parameters in later iterations
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("backend-txt","carrot.report.quantify.height","carrot.processing.replacement.simple"))
class PositiveModeTargetedWorkflow015ISTDMSDialsVerificationTest extends WordSpec with LazyLogging {
  @Autowired
  val workflow: LCMSPositiveModeTargetWorkflow[Double] = null

  @Autowired
  val listener: TestWorkflowEventListener = null

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val acquisitionLoader:AcquisitionLoader = null

  @Autowired
  val targetLibrary: LibraryAccess[Target] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  //sample name to test
  val sampleName = "B5_P20Lipids_Pos_QC000.abf"

  "LCMSPositiveModeTargetWorkflowTest" when {

    "ensure our targets are defined" in {
      assert(targetLibrary.load(acquisitionLoader.load(loader.getSample(sampleName)).get).nonEmpty)
    }

    s"find *015 1_MG 17:0/0:0/0:0 [M+Na]+ ISTD in sample $sampleName with a value over 80k" must {

      "process our single sample" in {
        val result = workflow.process(
          Experiment(
            classes = ExperimentClass(
              samples = loader.getSample(sampleName) :: List(),None) :: List(), None))
      }

      "has a result" in {
        assert(listener.quantifiedExperiment != null)
      }

      "result has content " in {
        assert(listener.quantifiedExperiment.classes.nonEmpty)
        assert(listener.quantifiedExperiment.classes.head.samples.nonEmpty)
      }


      "ensure we have targets defined" in {
        assert(listener.quantifiedExperiment.classes.head.samples.head.asInstanceOf[QuantifiedSample[Double]].quantifiedTargets.nonEmpty)
      }

      "validate the result" in {

        val sample: QuantifiedSample[Double] = listener.quantifiedExperiment.classes.head.samples.head.asInstanceOf[QuantifiedSample[Double]]

        val target = sample.quantifiedTargets.filter(_.name.get == "*015 1_MG 17:0/0:0/0:0 [M+Na]+ ISTD").head

        val value = target.quantifiedValue.get

        logger.info(s"value was: $value")
        value shouldBe (80000.0 +- 5000.0)
      }
    }

  }
}
