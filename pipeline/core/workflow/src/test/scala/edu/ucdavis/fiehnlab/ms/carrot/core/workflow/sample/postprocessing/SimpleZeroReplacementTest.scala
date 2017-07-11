package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.QuantifiedSample
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.LCMSTargetRetentionIndexCorrection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlg on 7/13/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("common"))
class SimpleZeroReplacementTest extends WordSpec {

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
      }
    }

  }
}
