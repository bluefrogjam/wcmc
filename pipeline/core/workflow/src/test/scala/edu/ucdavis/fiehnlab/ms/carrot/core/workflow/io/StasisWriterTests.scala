package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.storage.StasisResultStorage
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.ZeroReplacement
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client.StasisClient
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model._
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.mockito.{InjectMocks, Mock}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.collection.JavaConverters._

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("carrot.report.quantify.height", "carrot.processing.replacement.simple",
  "carrot.lcms", "carrot.processing.peakdetection", "file.source.luna", "carrot.output.storage.aws",
  "test"))
class StasisWriterTests extends WordSpec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar with LazyLogging {
  val libName = "lcms_istds"

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

  @Mock
  val stasis_cli: StasisClient = null

  @Autowired
  @InjectMocks
  val writer: StasisResultStorage[Double] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  import org.mockito.MockitoAnnotations

  override def beforeEach(): Unit = {
    logger.info("initiating mocks")
    MockitoAnnotations.initMocks(this)
  }

  "StasisWriter " should {
    val sample = sampleLoader.loadSample("B5_P20Lipids_Pos_NIST01.mzml").get
    val method = AcquisitionMethod(ChromatographicMethod(libName, Some("test"), Some("test"), Some(PositiveMode())))

    val result = quantification.process(
      annotation.process(
        correction.process(
          deconv.process(sample, method),
          method),
        method),
      method)

    "have quantified data" in {
      logger.info(s"QUANTIFIED: ${result.quantifiedTargets.size}")
      result.quantifiedTargets should not be empty
    }

    "have a stasisWriter" in {
      stasis_cli should not be null
      stasis_cli shouldBe a[StasisClient]
      writer.stasis_cli should not be null
      writer.stasis_cli shouldBe a[StasisClient]
    }

    "send the result of a sample to stasis" in {
      when(stasis_cli.addTracking(TrackingData(sample.name, "processing", sample.fileName))).thenReturn(ResponseEntity.ok(mock[TrackingResponse]))
      when(stasis_cli.addResult(mock[ResultData])).thenReturn(ResponseEntity.ok(mock[ResultData]))

      val data = writer.save(result)

      data.injections should have size 1

      logger.info("INJECTIONS: " + data.injections.keySet().asScala.mkString(";"))
      val injections = data.injections.asScala
      injections(sample.name) shouldBe an[Injection]
      injections(sample.name).results.length should be > 0

      writer.stasis_cli.getTracking(sample.name).status.maxBy(_.priority).value === "PROCESSING"
    }
  }
}

@Configuration
@Import(Array(classOf[RestClientConfig], classOf[TargetedWorkflowTestConfiguration]))
class StatisWriterTestConfig extends MockitoSugar {
  @Bean
  def writer: StasisResultStorage[Double] = new StasisResultStorage[Double]()

}
