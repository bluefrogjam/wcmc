package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.runner

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.DelegateLibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget}
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager, TestPropertySource}

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[RunnerTestConfig]))
@ActiveProfiles(Array("test", "carrot.lcms",
  "carrot.output.writer.aws", "file.source.eclipse",
  "file.source.luna", "carrot.resource.store.bucket"))
@TestPropertySource(properties = Array(
  "CARROT_SAMPLE:BioRec_LipidsPos_PhIV_001a.mzml",
  "CARROT_METHOD:jenny-tribe | test | test | positive",
  "CARROT_MODE:lcms",
  "carrot.submitter:dpedrosa@ucdavis.edu"
))
class TestJennySample extends WordSpec with ShouldMatchers with LazyLogging with BeforeAndAfter {

  @Autowired
  val runner: Runner = null

  @Autowired
  val stasis_cli: StasisService = null

  @Autowired
  val ctx: ApplicationContext = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  before {
    logger.info(ctx.getBeanDefinitionNames.mkString("\n"))
  }


  "a runner" should {
    "have a correction library" in {
      val clib = ctx.containsBean("correctionLibrary").asInstanceOf[DelegateLibraryAccess[CorrectionTarget]]
      clib.load(AcquisitionMethod.deserialize("jenny-tribe | test | test | positive")) should have size 25
      clib.load(AcquisitionMethod.deserialize("jenny-tribe | test | test | negative")) should have size 11
    }

    "have an annotation library" in {
      val alib = ctx.containsBean("annotationLibrary").asInstanceOf[DelegateLibraryAccess[AnnotationTarget]]
      alib.load(AcquisitionMethod.deserialize("jenny-tribe | test | test | positive")) should have size 1016
      alib.load(AcquisitionMethod.deserialize("jenny-tribe | test | test | negative")) should have size 1110
    }

    "have a stasis client" in {
      stasis_cli should not be null
    }

    "process a sample" in {
      runner.run()
    }

    "have results on aws" in {
      stasis_cli.getResults("LipidsPos_PhV14_021_265096") should not be null
    }
  }
}
