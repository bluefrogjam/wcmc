package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{GapFilledTarget, QuantifiedSample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.io.ResourceLoaderSampleLoader
import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * This test is a case to ensure that our value is in the exspected range and this annotation
  * won't change based on configured parameters in later iterations
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("backend-txt", "carrot.report.quantify.height", "carrot.processing.replacement.simple"))
class PositiveModeTargetedWorkflow015ISTDVerificationTest extends WordSpec with LazyLogging {
  @Autowired
  val workflow: LCMSPositiveModeTargetWorkflow[Double] = null

  @Autowired
  val listener: TestWorkflowEventListener = null

  @Autowired
  val loader: ResourceLoaderSampleLoader = null

  @Autowired
  val targetLibrary: LibraryAccess[Target] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  //sample name to test
  val sampleName = "B5_P20Lipids_Pos_Blank001.abf"

  "LCMSPositiveModeTargetWorkflowTest" when {

    "ensure our targets are defined" in {
      assert(targetLibrary.load(AcquisitionMethod(None)).nonEmpty)
    }

    "able to load our sample" in {
      assert(loader.loadSample(sampleName).isDefined)
    }

    s"find *015 1_MG 17:0/0:0/0:0 [M+Na]+ ISTD in sample $sampleName with a value over 80k" must {

      "process our single sample" in {
        val result = workflow.process(
          Experiment(
            classes = ExperimentClass(
              samples = loader.getSample(sampleName) :: List(), None
            ) :: List(), None, AcquisitionMethod(None))
        )

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

      "validate the amount of replaced value" in {
        val count = listener.quantifiedExperiment.classes.head.samples.head.asInstanceOf[QuantifiedSample[Double]].quantifiedTargets.count(_.isInstanceOf[GapFilledTarget[Double]])
        logger.info(s"replaced value count: ${count}")

        count shouldBe (370)
      }
      "validate the result" in {

        val sample: QuantifiedSample[Double] = listener.quantifiedExperiment.classes.head.samples.head.asInstanceOf[QuantifiedSample[Double]]

        val target = sample.quantifiedTargets.filter(_.name.get == "*015 1_MG 17:0/0:0/0:0 [M+Na]+ ISTD").head

        val value = target.quantifiedValue.get

        logger.info(s"value was: $value")
        value shouldBe (80000.0 +- 1000.0)
      }
    }

  }
}
