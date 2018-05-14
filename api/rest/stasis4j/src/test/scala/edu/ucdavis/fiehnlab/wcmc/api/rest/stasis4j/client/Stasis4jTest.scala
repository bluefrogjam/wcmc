package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client

import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.model._
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.collection.JavaConverters._

@RunWith(classOf[SpringRunner])
@SpringBootTest
class Stasis4jTest extends WordSpec with ShouldMatchers with LazyLogging {

  @Autowired
  val client: StasisClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "StasisClientTest" should {
    val filename = s"test${new Date().getTime}"
    val delay = 1000

    "create/get Acquisition" in {
      val metadata = SampleData(filename,
        Acquisition("instrument A", "GCTOF", "positive", "gcms"),
        Metadata("123456", "rat", "tissue"),
        Userdata("file123", ""))

      val res = client.createAcquisition(metadata)
      res.getStatusCode === 200

      Thread.sleep(delay)
      val res2 = client.getAcquisition(metadata.sample)

      res2 should not be null
      res2.id should equal(metadata.sample)
      res2.metadata should equal(metadata.metadata)
      res2.acquisition should equal(metadata.acquisition)
    }

    "create/get AquisitionFromMinix" in {
      // MX = 297319
      val res = client.createAquisitionFromMinix("http://minix.fiehnlab.ucdavis.edu/rest/export/297319")
      res.getStatusCode === 200

      val res2 = client.getResults("180510edpsa02_1")
      res2.sample should equal("180510edpsa02_1")

    }

    "add/get Tracking" in {
      val res = client.addTracking(filename, "entered")
      res.getStatusCode === 200
      logger.debug(s"track response: ${res.getBody.toString}")

      Thread.sleep(delay)

      val res2 = client.getTracking(filename)
      res2.id should equal(filename)
      res2.status.head.value should equal("entered")
    }

    "add/get Result" in {
      val data = ResultData(filename,
        Map[String, Injection](
          "test_1" -> Injection("R2D2",
            Correction(5, "test",
              Array(Curve(121.12, 121.2),
                Curve(123.12, 123.2))
            ),
            Array(
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
