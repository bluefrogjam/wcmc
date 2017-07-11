package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, SampleLoader, TxtStreamLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowProperties
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
@ActiveProfiles(Array("common"))
class PositiveModeTargetedWorkflow015ISTDVerificationTest extends WordSpec with LazyLogging {
  @Autowired
  val workflow: LCMSPositiveModeTargetWorkflow[Double] = null

  @Autowired
  val properties: WorkflowProperties = null

  @Autowired
  val listener: TestWorkflowEventListener = null

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val targetLibrary: LibraryAccess[Target] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  //sample name to test
  val sampleName = "Pos_QC005.mzXML"

  "LCMSPositiveModeTargetWorkflowTest" when {

    "ensure our targets are defined" in {
      assert(targetLibrary.load.nonEmpty)
    }

    "able to load our sample" in{
      assert(loader.loadSample(sampleName).isDefined)
    }

    s"find *015 1_MG 17:0/0:0/0:0 [M+Na]+ ISTD in sample $sampleName with a value over 80k" must {

      "process our single sample" in {
        val result = workflow.process(
          Experiment(
            classes = ExperimentClass(
              samples = loader.getSample(sampleName) :: List()) :: List(), None))

      }

      "has a result" in {
        assert(listener.quantifiedExperiment != null)
      }

      "result has coptent " in {
        assert(listener.quantifiedExperiment.classes.nonEmpty)
        assert(listener.quantifiedExperiment.classes.head.samples.nonEmpty)
      }


      "ensure we have targets defined" in {
        assert(listener.quantifiedExperiment.classes.head.samples.head.asInstanceOf[QuantifiedSample[Double]].quantifiedTargets.nonEmpty)
      }

      "validate the result" in {

        val sample:QuantifiedSample[Double] = listener.quantifiedExperiment.classes.head.samples.head.asInstanceOf[QuantifiedSample[Double]]

        val target = sample.quantifiedTargets.filter(_.target.name.get == "*015 1_MG 17:0/0:0/0:0 [M+Na]+ ISTD").head

        val value = target.quantifiedValue.get

        logger.info(s"value was: $value")
        value shouldBe (80000.0 +- 1000.0)
      }
    }

  }
}
