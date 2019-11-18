package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{NegativeMode, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.Workflow
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration}
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
    "carrot.processing.dump.spectra",
    "carrot.nostasis"

  ))
class DumpAnnotationInformationTest extends WordSpec {

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val dumpAction: DumpAnnotationInformation = null
  @Autowired
  val workflow: Workflow[Double] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)
  "DumpAnnotationInformationTest" should {

    "run" in {
      val sample: Sample = loader.getSample("B2b_SA1594_TEDDYLipids_Neg_MSMS_1U2WN.mzml")
      val method = AcquisitionMethod(ChromatographicMethod("teddy", Some("6550"), Some("test"), Option(NegativeMode())))

      val result = workflow.process(sample, method, Some(sample))
    }

  }
}

@Configuration
class DumpConfiguration {

  @Bean
  def workflow: Workflow[Double] = new Workflow[Double]
}
