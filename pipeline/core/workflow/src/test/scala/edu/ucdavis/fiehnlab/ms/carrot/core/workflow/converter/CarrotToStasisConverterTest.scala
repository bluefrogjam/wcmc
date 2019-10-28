package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.converter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{MergeLibraryAccess, SampleLoader}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{NegativeMode, Sample, Target => CTarget}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.ZeroReplacementProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.{Target => STTarget}
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}


@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(
  Array("test",
    "carrot.lcms",
    "file.source.eclipse",
    "carrot.processing.peakdetection",
    "carrot.processing.replacement.mzrt",
    "carrot.report.quantify.height",
    "carrot.targets.yaml.annotation",
    "carrot.targets.yaml.correction",
    "carrot.output.storage.converter.target"
  ))
class CarrotToStasisConverterTest extends WordSpec with Logging with Matchers {
  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val deconv: PeakDetection = null

  @Autowired
  val correction: CorrectionProcess = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

  @Autowired
  val quantification: QuantifyByHeightProcess = null

  @Autowired
  val targets: MergeLibraryAccess = null

  @Autowired
  val replacementProperties: ZeroReplacementProperties = null

  @Autowired
  val converter: CarrotToStasisConverter = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "SampleToMapConverterTest" should {
    val sampleNames = Seq("B7B_TeddyLipids_Neg_QC015.mzml", "B12A_SA11202_TeddyLipids_Neg_1RXZX_2.mzml").reverse
    val samples = loader.getSamples(sampleNames)
    val method = AcquisitionMethod(ChromatographicMethod("teddy", Some("6550"), Some("test"), Some(NegativeMode())))
    val library = Seq(targets.correctionLibraries(method), targets.annotationLibraries(method)).flatten


    val expClass = ExperimentClass(samples, None)
    val experiment = Experiment(Seq(expClass), None, method)
    val qsample = samples.collect {
      case sample: Sample =>
        quantification.process(
          annotation.process(
            correction.process(
              deconv.process(sample, method, None),
              method, None),
            method, None),
          method, Some(sample))
    }

    "convert a Quantified sample into a Stasis sample" in {
      val stasisTargets: Seq[STTarget] = qsample.head.quantifiedTargets.collect {
        case target: CTarget => converter.asStasisTarget(target)
      }.sortBy(t => t.index)

      val stasisTargets2: Seq[STTarget] = qsample.reverse.head.quantifiedTargets.collect {
        case target: CTarget => converter.asStasisTarget(target)
      }.sortBy(t => t.index)


      stasisTargets.size should be(library.size)
      stasisTargets should equal(stasisTargets2)
    }
  }
}
