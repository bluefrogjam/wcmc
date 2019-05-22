package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSample, PositiveMode, QuantifiedSample, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlg on 7/1/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("carrot.report.quantify.height", "carrot.processing.peakdetection", "carrot.lcms", "carrot.lcms.correction", "file.source.luna", "test","carrot.targets.yaml.annotation","carrot.targets.yaml.correction"))
class QuantifyByHeightProcessTest extends WordSpec with Matchers with Logging {
  val libName = "lcms_istds"

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

  @Autowired
  val quantification: QuantifyByHeightProcess = null

  @Autowired
  val deco: PeakDetection = null

  @Autowired
  val stasis_cli: StasisService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "QuantifyByHeightProcessTest" should {

    val method = AcquisitionMethod(ChromatographicMethod(libName, Some("test"), Some("test"), Option(PositiveMode())))

    val samples: Seq[_ <: Sample] = loader.getSamples(Seq("B5_P20Lipids_Pos_NIST02.mzml", "B5_SA0002_P20Lipids_Pos_1FL_1006.mzml"))

    //compute purity values
    val purityComputed = samples //.map(purity.process)

    //correct the data
    val correctedSample = purityComputed.map((item: Sample) => correction.process(deco.process(item, method, None), method, None))

    val annotated = correctedSample.map((item: CorrectedSample) => annotation.process(item, method, None))

    annotated.foreach { sample =>

      s"process ${sample}" in {

        val result: QuantifiedSample[Double] = quantification.process(sample, method, None)

        var annotationCount = 0
        result.quantifiedTargets.foreach { a =>
          if (a.spectra.isDefined) {
            annotationCount = annotationCount + 1
          }
        }

        //make sure that we the same amount of annotations as spectra
        assert(annotationCount == sample.spectra.size)

        stasis_cli.getTracking(sample.name).status.map(_.value) should contain("deconvoluted")
        stasis_cli.getTracking(sample.name).status.map(_.value) should contain("corrected")
        stasis_cli.getTracking(sample.name).status.map(_.value) should contain("annotated")
        stasis_cli.getTracking(sample.name).status.map(_.value) should contain("quantified")
      }
    }
  }
}
