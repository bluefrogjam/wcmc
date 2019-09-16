package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.{CorrectionObjective, CorrectionSettings, HyperoptTestConfiguration, Statistics}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.TestContextManager

@RunWith(classOf[JUnitRunner])
class GaussianSimilarityCorrectionLossFunctionTest extends CorrectionLossFunctionTest {

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "GaussianSimilarityCorrectionLossFunctionTest" should {

    "apply-qtof-csh" in {
      val correctionObjective = new CorrectionObjective(
        classOf[HyperoptTestConfiguration],
        Array("file.source.eclipse", "carrot.report.quantify.height", "carrot.processing.peakdetection", "carrot.lcms", "test", "carrot.targets.yaml.annotation", "carrot.targets.yaml.correction"),
        new GaussianSimilarityCorrectionLossFunction(),
        CSH_SAMPLES,
        CSH_METHOD,
        Seq.empty
      )

      val tightResult = correctionObjective.apply(CSH_CORRECTION_TIGHT_PARAMS)
      val narrowResult = correctionObjective.apply(CSH_CORRECTION_NARROW_PARAMS)


      println(tightResult)
      println(narrowResult)
      assert(narrowResult < tightResult)
    }
  }
}
