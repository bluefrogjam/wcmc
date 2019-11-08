package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client

import java.util.Date

import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model._
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.web.client.HttpClientErrorException

import scala.collection.JavaConverters._

@RunWith(classOf[SpringRunner])
@ActiveProfiles(Array("test"))
@SpringBootTest(classes = Array(classOf[StasisTestConfiguration]))
class Stasis4jTest extends WordSpec with Matchers with Logging with Eventually {

  @Autowired
  val client: StasisClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "StasisService Integration Tests" should {

    val filename = s"test_${new Date().getTime}"
    val delay = 1000

    "create/get Acquisition" in {
      val metadata = SampleData(filename,
        Acquisition("instrument A", "positive", "gcms"),
        Processing("my gcms method name"),
        Metadata("123456", "rat", "tissue"),
        Userdata("file123", ""), Array.empty, experiment = "none")

      val res = client.createAcquisition(metadata)
      res.getStatusCode === 200

      Thread.sleep(delay)
      val res2 = client.getAcquisition(metadata.sample).get

      res2 should not be null
      res2.id should equal(metadata.sample)
      res2.metadata should equal(metadata.metadata)
      res2.acquisition should equal(metadata.acquisition)
    }

    "create/get Acquisition with reference data" in {
      val metadata = SampleData(s"test_${new Date().getTime}",
        Acquisition("instrument B", "positive", "lcms super method"),
        Processing("my gcms method name"),
        Metadata("123456", "rat", "tissue"),
        Userdata("file123", ""),
        Array(Reference("ref1", "value1"),
          Reference("ref2", "value2"))
        , experiment = "none"
      )

      val res = client.createAcquisition(metadata)
      res.getStatusCode === 200

      Thread.sleep(delay)
      val res2 = client.getAcquisition(metadata.sample).get

      res2 should not be null
      res2.id should equal(metadata.sample)
      res2.acquisition.instrument should equal("instrument B")
      res2.references.length should be > 0
      res2.references(0) should equal(Reference("ref1", "value1"))
    }

    "schedule a sample" must {
      val name = s"test_${new Date().getTime}"
      "first upload a sample" in {

        val metadata = SampleData(name,
          Acquisition("instrument B", "positive", "lcms super method"),
          Processing("my gcms method name"),
          Metadata("123456", "rat", "tissue"),
          Userdata("file123", ""),
          Array(Reference("ref1", "value1"),
          Reference("ref2", "value2")),
          experiment = "none"
        )

        val res = client.createAcquisition(metadata)
        res.getStatusCode === 200

        Thread.sleep(delay)
      }

      "and then schedule it" in {
        val res2 = client.schedule(name, "lcms super method", "lcms", "test")
        logger.info(s"RESPONSE: ${res2}")
        res2.getStatusCode should be(HttpStatus.OK)
      }
    }

    "add/get Tracking" in {
      val data = TrackingData(
        filename.split("\\.").head,
        "processing",
        filename
      )

      logger.info(s"adding tracking for ${data.sample}")

      val res = client.addTracking(data)
      res.getStatusCode === 200
      logger.debug(s"track response: ${res.getBody.toString}")

      Thread.sleep(delay)

      val res2 = client.getTracking(filename).get
      res2.id should equal(filename)
      res2.status.map(_.value) should contain("processing")
    }

    "delete tracking" in {
      val sample = filename.split("\\.").head
      client.deleteTracking(sample)
      logger.info(s"Deleted")

      client.getTracking(sample) shouldBe None
    }

    "schedule a sample" in {
      val res2 = client.schedule("B5_P20Lipid_Pos_NIST001.mzml", "lcms_istds | test | test | positive", "carrot.lcms", "test")
      logger.info(s"RESPONSE: ${res2}")
      res2.getStatusCode should be(HttpStatus.OK)
    }

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class StasisTestConfiguration
