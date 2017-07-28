package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{GapFilledSpectra, QuantifiedSample}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.LCMSTargetRetentionIndexCorrection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.test.context.{SpringBootTest, TestConfiguration}
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlg on 7/13/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
class SimpleZeroReplacementTest extends WordSpec with LazyLogging with ShouldMatchers{

  @Autowired
  val simpleZeroReplacement: SimpleZeroReplacement = null

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrection = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

  @Autowired
  @Qualifier("quantification")
  val quantify: QuantifyByHeightProcess = null

  @Autowired
  val loader:SampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "SimpleZeroReplacementTest" must {

    val sample:QuantifiedSample[Double] =
      quantify.process(
        annotation.process(
          correction.process(
            loader.getSample("B5_P20Lipids_Pos_QC000.abf")
          )
        )
      )

    "replaceValue" should {

      var replaced:QuantifiedSample[Double] = null
      "replace the null values in the file" in {
          replaced = simpleZeroReplacement.process(sample)


	      replaced.spectra match {
		      case x: GapFilledSpectra[Double] => logger.info(s"spectra: ${x}")
        }

        //all spectra should be the same count as the targets
        replaced.spectra.size should be(replaced.quantifiedTargets.size)
      }
    }

  }
}

@TestConfiguration
class GapFillingConfigTest {

	@Bean
	def simpleZeroReplacement(properties: WorkflowProperties): SimpleZeroReplacement = new SimpleZeroReplacement(properties)
}
