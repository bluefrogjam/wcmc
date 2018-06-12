package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.ZeroReplacement
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client.StasisClient
import org.junit.runner.RunWith
import org.mockito.Mock
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
//@ActiveProfiles(Array("carrot.report.quantify.height", "carrot.processing.replacement.simple", "carrot.lcms", "carrot.processing.peakdetection", "file.source.luna"))
@ActiveProfiles(Array("file.source.luna"))
class StasisWriterTests extends WordSpec with ShouldMatchers with LazyLogging {
//  @Mock
//  var stasis_cli: StasisClient = _

//  @Autowired
//  val deconv: PeakDetection = null
//
//  @Autowired
//  val correction: CorrectionProcess = null
//
//  @Autowired
//  val annotation: LCMSTargetAnnotationProcess = null
//
//  @Autowired
//  val quantification: QuantifyByHeightProcess = null
//
//  @Autowired
//  val replacement: ZeroReplacement = null
//
//  @Autowired
//  val writer: StasisWriter[Double] = null

  @Autowired
  val sampleLoader: SampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "StasisWriter " should {
    val sample = sampleLoader.loadSample("testA.mzml").get
//    val method = AcquisitionMethod(ChromatographicMethod("lcms_istds", Some("test"), Some("test"), Some(PositiveMode())))

//    def writer: StasisWriter[Double] = new StasisWriter[Double]

//    val result = quantification.process(
//      annotation.process(
//        correction.process(
//          deconv.process(sample, method),
//          method),
//        method),
//      method)

//    "have a stasisWriter" in {
//      writer.stasis_cli should not be null
//    }

//    "send the result of a sample to stasis" in {
//      writer.save(result) === "1"
//    }
  }
}
