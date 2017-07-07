package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, SampleLoader}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSample, Sample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.RetentionIndexCorrectionProperties
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
@ActiveProfiles(Array("common", "msdial"))
class PositiveModeTargetedWorkflow015ISTDMSDialsVerificationTest extends WordSpec with LazyLogging {
  @Autowired
  val workflow: LCMSPositiveModeTargetWorkflow[Double] = null

  @Autowired
  val properties: WorkflowProperties = null

  @Autowired
  val lcmsProperties: RetentionIndexCorrectionProperties = null

  @Autowired
  val listener: TestWorkflowEventListener = null

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val targetLibrary: LibraryAccess[Target] = null

	new TestContextManager(this.getClass).prepareTestInstance(this)

  //sample name to test
  val sampleName = "B5_P20Lipids_Pos_QC000.abf"

  "LCMSPositiveModeTargetWorkflowTest" when {

    "configure out settings " in {
      lcmsProperties.minimumFoundStandards = 15
    }

    "ensure our targets are defined" in {
      assert(targetLibrary.load.nonEmpty)
    }

//  possibly add an endpoint to check file presence on the server
//    "able to load our sample" in{
//      assert(loader.loadSample(sampleName).isDefined)
//    }

    s"find *015 1_MG 17:0/0:0/0:0 [M+Na]+ ISTD in sample $sampleName with a value over 80k" must {

      "process our single sample" in {
        val result = workflow.process(
          Experiment(
            classes = ExperimentClass(
              samples = new Sample {override val spectra: Seq[_ <: MSSpectra] = Seq.empty
                override val fileName: String = sampleName
              } :: List()) :: List(), None))
        assert(result.available() > 10)
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

        val sample:QuantifiedSample[Double] = listener.quantifiedExperiment.classes.head.samples.head.asInstanceOf[QuantifiedSample[Double]]

        val target = sample.quantifiedTargets.filter(_.target.name.get == "*015 1_MG 17:0/0:0/0:0 [M+Na]+ ISTD").head

        val value = target.quantifiedValue.get

        logger.info(s"value was: $value")
        value shouldBe (80000.0 +- 1000.0)
      }
    }

  }
}
