package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

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
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client.StasisClient
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model._
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.mockito.{InjectMocks, Mock, MockitoAnnotations}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
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
  "test", "teddy"))
class StasisResultStorageTests extends WordSpec with Matchers with BeforeAndAfterEach with MockitoSugar with Logging {
  val libName = "teddy"

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
  val mockStasis: StasisClient = MockitoSugar.mock[StasisClient](CALLS_REAL_METHODS)

  @Autowired
  val stasis_cli: StasisService = null

  @Autowired
  @InjectMocks
  val writer: StasisResultStorage[Double] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  override def beforeEach(): Unit = {
    logger.info("initiating mocks")
    MockitoAnnotations.initMocks(this)
  }

  "StasisResultStorage" should {
    val sample = sampleLoader.loadSample("B2a_TEDDYLipids_Neg_QC006.mzml").get
    val method = AcquisitionMethod(ChromatographicMethod(libName, Some("6550"), Some("test"), Some(NegativeMode())))

    val result = quantification.process(
      annotation.process(
        correction.process(
          deconv.process(sample, method, Some(sample)),
          method, Some(sample)),
        method, Some(sample)),
      method, Some(sample))

    "have quantified data" in {
      logger.info(s"QUANTIFIED: ${result.quantifiedTargets.size}")
      result.quantifiedTargets should not be empty
    }

    "have a stasisWriter" in {
      mockStasis should not be null
      mockStasis shouldBe a[StasisClient]
      writer.stasis_cli should not be null
      writer.stasis_cli shouldBe a[StasisClient]
    }

    "send the result of a sample to stasis" in {
      when(mockStasis.addTracking(TrackingData(sample.name, "exported", sample.fileName))).thenReturn(ResponseEntity.ok(mock[TrackingResponse]))
      when(mockStasis.addResult(mock[ResultData])).thenReturn(ResponseEntity.ok(mock[ResultData]))

      val data = writer.save(result)
      data.injections should have size 1

      val injections = data.injections.asScala
      injections(sample.name) shouldBe an[Injection]

      writer.stasis_cli.getTracking(sample.name).status.maxBy(_.priority).value.toLowerCase === "exported"

      var results: ResultResponse = null
      try {
        results = stasis_cli.getResults(sample.name)
      } catch {
        case ex: Exception =>
          logger.error(ex.getMessage, ex)
      }

      results should have(
        'sample (sample.name),
        'id (sample.name)
      )

    }
  }
}

@Configuration
@Import(Array(classOf[RestClientConfig], classOf[TargetedWorkflowTestConfiguration]))
class StatisWriterTestConfig extends MockitoSugar {
  @Bean
  def writer: StasisResultStorage[Double] = new StasisResultStorage[Double]()

}
