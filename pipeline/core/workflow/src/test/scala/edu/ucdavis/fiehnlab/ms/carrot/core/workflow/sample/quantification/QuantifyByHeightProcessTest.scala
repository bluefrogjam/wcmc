package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification

import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{MergeLibraryAccess, SampleLoader}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectedSample, PositiveMode, QuantifiedSample, Sample}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlg on 7/1/2016.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("test",
  "carrot.lcms",
  "file.source.eclipse",
  "carrot.lcms.correction",
  "carrot.report.quantify.height",
  "carrot.processing.peakdetection",
  "carrot.targets.yaml.annotation",
  "carrot.targets.yaml.correction"))
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
  val library: MergeLibraryAccess = null

  @Autowired
  val stasis_cli: StasisService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "QuantifyByHeightProcessTest" should {
    val samplenames: Seq[String] = Seq("B5_P20Lipids_Pos_NIST02.mzml", "B5_SA0002_P20Lipids_Pos_1FL_1006.mzml")
    val samples: Seq[_ <: Sample] = loader.getSamples(samplenames)

    val method = AcquisitionMethod(ChromatographicMethod(libName, Some("test"), Some("test"), Option(PositiveMode())))

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

        //make sure that we have the same amount of annotations as spectra
        assert(annotationCount == sample.spectra.size)
      }

      s"have the same amount of quantified targets than the library in ${sample}" in {
        val targets = library.load(method)

        val result: QuantifiedSample[Double] = quantification.process(sample, method, None)

        result.quantifiedTargets should have size targets.size
      }
    }

    "have different annotations in 2 different samples" in {
      val results: Seq[QuantifiedSample[Double]] = Seq(
        quantification.process(annotated.head, method, None),
        quantification.process(annotated.reverse.head, method, None)
      )

      results.head.spectra should not equal results.reverse.head.spectra
    }

    "have equal targets in 2 different samples" in {
      val results: Seq[QuantifiedSample[Double]] = Seq(
        quantification.process(annotated.head, method, None),
        quantification.process(annotated.reverse.head, method, None)
      )

      results.head.quantifiedTargets.map(t => (t.idx, t.name, t.retentionIndex, t.accurateMass)) should equal(results.reverse.head.quantifiedTargets.map(t => (t.idx, t.name, t.retentionIndex, t.accurateMass)))
    }
  }
}
