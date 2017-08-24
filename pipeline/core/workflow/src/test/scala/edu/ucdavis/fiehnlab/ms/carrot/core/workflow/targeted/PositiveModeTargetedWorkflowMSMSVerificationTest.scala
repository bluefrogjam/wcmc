package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.targeted

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.acquisition.AcquisitionLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, TxtStreamLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{GapFilledTarget, QuantifiedSample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.io.ResourceLoaderSampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowProperties
import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}


@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("dynamic-library", "backend-mona", "quantify-by-height"))
class PositiveModeTargetedWorkflowMSMSGenerationVerificationWithMonaTest extends WordSpec with LazyLogging {

  @Autowired
  val resourceLoader: DelegatingResourceLoader = null

  @Autowired
  val workflow: LCMSPositiveModeTargetWorkflow[Double] = null

  @Autowired
  val properties: WorkflowProperties = null

  @Autowired
  val listener: TestWorkflowEventListener = null

  @Autowired
  val loader: ResourceLoaderSampleLoader = null

  @Autowired
  val targetLibrary: LibraryAccess[Target] = null

  @Autowired
  val acquisitionLoader: AcquisitionLoader = null

  @Autowired
  val monaSpectrumRestClient: MonaSpectrumRestClient = null

  @Value("${mona.rest.server.user}")
  val username: String = null

  @Value("${mona.rest.server.password}")
  val password: String = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  //sample name to test
  val sampleNames = "B5_SA0267_P20Lipids_Pos_1FV_2416_MSMS.abf" :: "B5_SA0262_P20Lipids_Pos_1FV_2404_MSMS.abf" :: List()

  "PositiveModeTargetedWorkflowMSMSVerificationTest" when {

    "populate a target library" in {
      logger.warn("building MONA library")
      monaSpectrumRestClient.login(username, password)
      monaSpectrumRestClient.list().foreach(p => monaSpectrumRestClient.delete(p.id))

      val lib = new TxtStreamLibraryAccess[Target](resourceLoader.loadAsFile("targets.txt").get, "\t")
      val method = acquisitionLoader.load(loader.getSample(sampleNames.head)).get
      val targetsToAdd = lib.load(method)
      targetLibrary.add(targetsToAdd, method)

    }
    "ensure our targets are defined" in {
      assert(targetLibrary.load(acquisitionLoader.load(loader.getSample(sampleNames.head)).get).nonEmpty)
    }

    "able to load our sample" in {
      sampleNames.foreach { sampleName =>
        assert(loader.loadSample(sampleName).isDefined)
      }

    }

    s"process our samples" must {

      "process our samples" in {
        val result = workflow.process(
          Experiment(
            classes = ExperimentClass(
              samples = sampleNames.map { sampleName =>
                loader.getSample(sampleName)
              }


            ) :: List(), None)
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

      "validate the generation of new target" in {
        val method = acquisitionLoader.load(loader.getSample(sampleNames.head)).get
        assert(targetLibrary.load(method).size == 1232)
      }
    }

  }
}

/**
  * This test is a case to ensure that our value is in the exspected range and this annotation
  * won't change based on configured parameters in later iterations
  */


@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("dynamic-library", "backend-mona", "quantify-by-height"))
class PositiveModeTargetedWorkflowMSMSVerificationWithMonaTest extends WordSpec with LazyLogging {

  @Autowired
  val resourceLoader: DelegatingResourceLoader = null

  @Autowired
  val workflow: LCMSPositiveModeTargetWorkflow[Double] = null

  @Autowired
  val properties: WorkflowProperties = null

  @Autowired
  val listener: TestWorkflowEventListener = null

  @Autowired
  val loader: ResourceLoaderSampleLoader = null

  @Autowired
  val targetLibrary: LibraryAccess[Target] = null

  @Autowired
  val acquisitionLoader: AcquisitionLoader = null

  @Autowired
  val monaSpectrumRestClient: MonaSpectrumRestClient = null

  @Value("${mona.rest.server.user}")
  val username: String = null

  @Value("${mona.rest.server.password}")
  val password: String = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  //sample name to test
  val sampleName = "B5_SA0267_P20Lipids_Pos_1FV_2416_MSMS.abf"

  "PositiveModeTargetedWorkflowMSMSVerificationTest" when {

    "populate a target library" in {
      logger.warn("building MONA library")
      monaSpectrumRestClient.login(username, password)
      monaSpectrumRestClient.list().foreach(p => monaSpectrumRestClient.delete(p.id))

      val lib = new TxtStreamLibraryAccess[Target](resourceLoader.loadAsFile("targets.txt").get, "\t")
      val method = acquisitionLoader.load(loader.getSample(sampleName)).get
      val targetsToAdd = lib.load(method)
      targetLibrary.add(targetsToAdd, method)

    }
    "ensure our targets are defined" in {
      assert(targetLibrary.load(acquisitionLoader.load(loader.getSample(sampleName)).get).nonEmpty)
    }

    "able to load our sample" in {
      assert(loader.loadSample(sampleName).isDefined)
    }

    s"find *015 1_MG 17:0/0:0/0:0 [M+Na]+ ISTD in sample $sampleName with a value over 80k" must {

      "process our single sample" in {
        val result = workflow.process(
          Experiment(
            classes = ExperimentClass(
              samples = loader.getSample(sampleName) :: List()
            ) :: List(), None)
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

        count shouldBe (256)
      }
      "validate the result" in {

        val sample: QuantifiedSample[Double] = listener.quantifiedExperiment.classes.head.samples.head.asInstanceOf[QuantifiedSample[Double]]

        val target = sample.quantifiedTargets.filter(_.name.get == "*015 1_MG 17:0/0:0/0:0 [M+Na]+ ISTD").head

        val value = target.quantifiedValue.get

        logger.info(s"value was: $value")
        value shouldBe (44000.0 +- 1000.0)
      }

      "validate the generation of new target" in {

      }
    }

  }
}
