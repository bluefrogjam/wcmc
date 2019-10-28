package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions

import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.{CorrectionObjective, HyperoptTestConfiguration}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.springframework.test.context.TestContextManager

@RunWith(classOf[JUnitRunner])
class PeakHeightRSDCorrectionLossFunctionTest extends CorrectionLossFunctionTest {

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "PeakHeightRSDCorrectionLossFunctionTest" ignore {

    "apply-qtof-csh" in {

      val correctionObjective = new CorrectionObjective(
        classOf[HyperoptTestConfiguration],
        Array("file.source.eclipse", "carrot.report.quantify.height", "carrot.processing.peakdetection", "carrot.lcms", "test", "carrot.targets.yaml.annotation", "carrot.targets.yaml.correction"),
        new PeakHeightRSDCorrectionLossFunction(),
        CSH_SAMPLES,
        CSH_METHOD,
        Seq.empty
      )

      val tightResult = correctionObjective.apply(CSH_CORRECTION_TIGHT_PARAMS)
      val wideResult = correctionObjective.apply(CSH_CORRECTION_WIDE_PARAMS)

      assert(wideResult > tightResult)
    }
  }
}
