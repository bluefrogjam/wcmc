package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import java.io.{File, FileOutputStream}

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.clazz.ExperimentClass
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.converter.{CarrotToStasisConverter, SampleToMapConverter}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.ZeroReplacement
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.Injection
import javax.annotation.PostConstruct
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("test",
  "carrot.lcms",
  "file.source.eclipse",
  "carrot.processing.peakdetection",
  "carrot.report.quantify.height",
  "carrot.processing.replacement.mzrt",
  "carrot.output.storage.converter.target",
  "carrot.output.storage.converter.sample",
  "carrot.targets.yaml.annotation",
  "carrot.targets.yaml.correction"
))
class QuantifiedSampleJsonWriterTest extends WordSpec with Matchers with Logging {
  @Autowired
  val deconv: PeakDetection = null

  @Autowired
  val correction: CorrectionProcess = null

  @Autowired
  val annotation: LCMSTargetAnnotationProcess = null

  @Autowired
  val quantification: QuantifyByHeightProcess = null

  @Autowired
  val replacement: ZeroReplacement = null

  @Autowired
  val sampleLoader: SampleLoader = null

  @Autowired
  val targetConverter: CarrotToStasisConverter = null

  @Autowired
  val converter: SampleToMapConverter[Double] = null

  @Autowired
  val ctx: ApplicationContext = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "LocalJsonResultStorage" should {
    val jsonWriter = new QuantifiedSampleJsonWriter[Double](converter)

    val method = AcquisitionMethod(ChromatographicMethod("csh", Some("6530"), Some("test"), Some(PositiveMode())))
    val sample = sampleLoader.getSample("Biorec001_posCSH_preFlenniken001.mzml")

    val quantified = quantification.process(
      annotation.process(
        correction.process(
          deconv.process(sample, method, Some(sample)),
          method, Some(sample)),
        method, Some(sample)),
      method, Some(sample))

    val experiment = Experiment(
      Seq(ExperimentClass(
        Seq(quantified),
        None
      )),
      Some("test"),
      method
    )

    "save the result data" in {

      val temp = new File(s"local_storage/${sample.name}.json")
//      temp.deleteOnExit()
      val out = new FileOutputStream(temp)

      jsonWriter.write(out, quantified)

      temp.length() should be > 0L
    }

  }

}
