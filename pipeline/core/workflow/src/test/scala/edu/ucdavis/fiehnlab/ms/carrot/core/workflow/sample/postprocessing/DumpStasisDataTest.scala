package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{NegativeMode, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@SpringBootTest
@ActiveProfiles(
  Array("test",
    "carrot.lcms",
    "file.source.eclipse",
    "carrot.report.quantify.height",
    "carrot.processing.peakdetection",
    "carrot.processing.replacement.mzrt",
    "carrot.targets.yaml.annotation",
    "carrot.targets.yaml.correction",
    "carrot.processing.dump.stasis",
    "carrot.stasis"

  ))
class DumpStasisDataTest extends WordSpec {


  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val dumpAction: DumpStasisData = null
  @Autowired
  val workflow: Workflow[Double] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)
  "DumpStasisDataTest" should {

    "doProcess" in {
      val sample: Sample = loader.getSample("B10A_SA9224_TeddyLipids_Neg_24H7R.mzml")
      val method = AcquisitionMethod(ChromatographicMethod("teddy", Some("6550"), Some("test"), Option(NegativeMode())))
      val result = workflow.process(sample, method, Some(sample))

    }

  }
}
