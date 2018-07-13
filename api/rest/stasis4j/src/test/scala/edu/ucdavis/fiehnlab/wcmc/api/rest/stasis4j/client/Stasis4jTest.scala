package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client

import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model._
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
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
class Stasis4jTest extends WordSpec with ShouldMatchers with LazyLogging {

  @Autowired
  val client: StasisClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "StasisClient Integration Tests" should {

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
      val res2 = client.getAcquisition(metadata.sample)

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
      val res2 = client.getAcquisition(metadata.sample)

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
            Reference("ref2", "value2"))
          , experiment = "none"
        )

        val res = client.createAcquisition(metadata)
        res.getStatusCode === 200

        Thread.sleep(delay)
      }

      "and than schedule it" in {
        val res2 = client.schedule(name, "lcms super method", "lcms", "test")
        //res2.getStatusCode === 200

        println(res2)
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

      val res2 = client.getTracking(filename)
      res2.id should equal(filename)
      res2.status.map(_.value) should contain("processing")
    }

    "delete tracking" in {
      val sample = filename.split("\\.").head
      client.deleteTracking(sample)
      logger.info(s"Deleted")

      val thrown = the[HttpClientErrorException] thrownBy client.getTracking(sample)
      thrown.getStatusCode should be(HttpStatus.NOT_FOUND)
    }

    "add/get Result" in {
      val data = ResultData(filename,
        Map[String, Injection](
          "test_1" -> Injection("R2D2",
            Correction(5, "test",
              curve = Array(Curve(121.12, 121.2), Curve(123.12, 123.2))
            ),
            results = Array(
              Result(
                Target(121.12, "test", "test_id", 12.2),
                Annotation(121.2, 10.0, replaced = false, 12.2)
              ),
              Result(
                Target(123.12, "test2", "test_id2", 132.12),
                Annotation(123.2, 103.0, replaced = true, 132.12)
              )
            )
          )
        ).asJava
      )

      val res = client.addResult(data)
      res.getStatusCode === 200

      logger.debug(s"result response: ${res.getBody.toString}")

      Thread.sleep(delay)

      val res2 = client.getResults(filename)
      logger.info(s"result response: ${res2}")

      res2.sample should equal(filename)
      res2.injections.size() should be >= 1
    }
  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class StasisTestConfiguration
