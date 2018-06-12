package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
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
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model.Injection
import org.junit.runner.RunWith
import org.scalatest.mock.MockitoSugar
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.collection.JavaConverters._

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("carrot.report.quantify.height", "carrot.processing.replacement.simple", "carrot.lcms", "carrot.processing.peakdetection", "file.source.luna", "carrot.output.writer.aws"))
class StasisWriterTests extends WordSpec with ShouldMatchers with LazyLogging {
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
  val stasis_cli: StasisClient = null

  @Autowired
  val writer: StasisWriter[Double] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "StasisWriter " should {
    val sample = sampleLoader.loadSample("B5_P20Lipids_Pos_NIST01.mzml").get
    val method = AcquisitionMethod(ChromatographicMethod("lcms_istds", Some("test"), Some("test"), Some(PositiveMode())))


    val result = quantification.process(
      annotation.process(
        correction.process(
          deconv.process(sample, method),
          method),
        method),
      method)

    "have a stasisWriter" in {
      stasis_cli should not be null
      writer.stasis_cli should not be null
    }

    "send the result of a sample to stasis" in {
      val data = writer.save(result)

      data.sample === "B5_P20Lipids_Pos_NIST01"

      data.injections should have size 1
      logger.info(data.injections.keySet().asScala.mkString(";"))
      val injections = data.injections.asScala
      injections("B5_P20Lipids_Pos_NIST01") shouldBe an[Injection]
      injections("B5_P20Lipids_Pos_NIST01").results.length should be >= 14
    }
  }
}

@Configuration
@Import(Array(classOf[RestClientConfig], classOf[TargetedWorkflowTestConfiguration]))
class StatisWriterTestConfig extends MockitoSugar {
  @Bean
  def stasis_cli: StasisClient = mock[StasisClient]
}
