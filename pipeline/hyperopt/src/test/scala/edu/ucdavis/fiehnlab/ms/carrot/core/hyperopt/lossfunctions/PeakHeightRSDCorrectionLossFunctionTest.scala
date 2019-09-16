package edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.lossfunctions

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.hyperopt.{CorrectionObjective, CorrectionSettings, HyperoptTestConfiguration, Statistics}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[JUnitRunner])
class PeakHeightRSDCorrectionLossFunctionTest extends CorrectionLossFunctionTest {

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "PeakHeightRSDCorrectionLossFunctionTest" should {

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
      val narrowResult = correctionObjective.apply(CSH_CORRECTION_NARROW_PARAMS)

      println(tightResult)
      println(narrowResult)
      assert(narrowResult < tightResult)
    }
  }
}
