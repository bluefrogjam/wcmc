package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io

import java.io.{FileOutputStream, OutputStream}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.CorrectionProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample._
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.PeakDetection
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.io.storage.StasisResultStorage
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.annotation.LCMSTargetAnnotationProcess
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing.{ZeroReplacement, ZeroreplacedTarget}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.quantification.QuantifyByHeightProcess
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.api.StasisService
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model._
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.mockito.{InjectMocks, Mock, MockitoAnnotations}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Configuration, Import}
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.collection.JavaConverters._

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("test",
  "carrot.lcms",
  "file.source.eclipse",
  "carrot.processing.peakdetection",
  "carrot.report.quantify.height",
  "carrot.processing.replacement.mzrt",
  "carrot.output.storage.aws",
  "carrot.output.storage.converter.target",
  "carrot.output.storage.converter.sample",
  "carrot.targets.yaml.annotation",
  "carrot.targets.yaml.correction"
  ))
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
  val mockStasis: StasisService = MockitoSugar.mock[StasisService](CALLS_REAL_METHODS)

  @Autowired
  val stasis_cli: StasisService = null

  @Autowired
  @InjectMocks
  val writer: StasisResultStorage[Double] = null

  @Autowired
  val objectMapper: ObjectMapper = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  override def beforeEach(): Unit = {
    logger.info("initiating mocks")
    MockitoAnnotations.initMocks(this)
  }


  val istds: Map[String, Double] = Map(
    "1_Ceramide (d18:1/17:0) iSTD [M+Cl]-_ICWGMOFDULMCFL-QKSCFGQVSA-N" -> 363,
    "1_Ceramide (d18:1/17:0) iSTD [M+FA-H]-_ICWGMOFDULMCFL-QKSCFGQVSA-N" -> 363,
    "1_CUDA iSTD [M-H]-_HPTJABJPZMULFH-UHFFFAOYSA-N" -> 45,
    "1_FA iSTD (16:0)-d3 [M-H]-_IPCSVZSSVZVIGE-FIBGUPNXSA-N" -> 189,
    "1_LPC (17:0) iSTD [M+FA-H]-_SRRQPVVYXBTRQK-XMMPIXPASA-N" -> 108.6,
    "1_LPE (17:1) iSTD [M-H]-_LNJNONCNASQZOB-HEDKFQSOSA-N" -> 77.4,
    "1_MAG (17:0/0:0/0:0) iSTD [M+FA-H]-_SVUQHVRAGMNPLW-UHFFFAOYSA-N" -> 183.6,
    "1_PC (12:0/13:0) iSTD [M+FA-H]-_FCTBVSCBBWKZML-WJOKGBTCSA-N" -> 214.2,
    "1_PE (17:0/17:0) iSTD [M-H]-_YSFFAUPDXKTJMR-DIPNUNPCSA-N" -> 380.4,
    "1_PG (17:0/17:0) iSTD [M-H]-_ZBVHXVKEMAIWQQ-QPPIDDCLSA-N" -> 336.6,
    "1_SM (d18:1/17:0) iSTD [M+FA-H]-_YMQZQHIESOAPQH-JXGHDCMNSA-N" -> 309.6
  )

  "StasisResultStorage" should {
    val sample = sampleLoader.getSample("B2a_TEDDYLipids_Neg_QC006.mzml")
    val method = AcquisitionMethod(ChromatographicMethod(libName, Some("6550"), Some("test"), Some(NegativeMode())))

    val quantified = quantification.process(
      annotation.process(
        correction.process(
          deconv.process(sample, method, Some(sample)),
          method, Some(sample)),
        method, Some(sample)),
      method, Some(sample))

    "have quantified data" in {
      logger.info(s"QUANTIFIED: ${quantified.quantifiedTargets.size}")
      quantified.quantifiedTargets should not be empty
    }

    "have a stasisWriter" in {
      mockStasis should not be null
      mockStasis shouldBe a[StasisService]
      writer.stasis_cli should not be null
      writer.stasis_cli shouldBe a[StasisService]
    }

    "send the result of a sample to stasis" in {
      when(mockStasis.addTracking(TrackingData(sample.name, "exported", sample.fileName))).thenReturn(ResponseEntity.ok(mock[TrackingResponse]))
      when(mockStasis.addResult(mock[ResultData])).thenReturn(ResponseEntity.ok(mock[ResultData]))

      val repl = quantified.quantifiedTargets.collect {
        case q: ZeroreplacedTarget => q
      }

      val data = writer.save(quantified)
      saveData(data)

      data.injections should have size 1

      logger.info(data.injections.keySet())

      val injections = data.injections.asScala
      injections(sample.name) shouldBe an[Injection]

      writer.stasis_cli.getTracking(sample.name).status.maxBy(_.priority).value.toLowerCase === "exported"

      var results: ResultResponse = null
      results = stasis_cli.getResults(sample.name)

      results should have(
        'sample (sample.name),
        'id (sample.name)
      )

      istds.foreach(istd => {
        val reportedtarget: Option[Result] = results.injections.get(sample.name).head.results.find(res => res.target.name.equals(istd._1))
        if (reportedtarget.isDefined)
          reportedtarget.get.target.retentionTimeInSeconds shouldBe istd._2 +- 0.01
      })
    }
  }

  def saveData(data: ResultData): Unit = {
    val sout: OutputStream = new FileOutputStream(s"${System.getProperty("user.home")}/.carrot_storage/${data.sample}.json")
    objectMapper.registerModule(DefaultScalaModule)
    objectMapper.writeValue(sout, data)

    sout.flush()
    sout.close()
    logger.info("Data saved")
  }
}

@Configuration
@Import(Array(classOf[RestClientConfig]))
class StatisWriterTestConfig {
}
