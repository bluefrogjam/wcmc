package edu.ucdavis.fiehnlab.wcmc.api.rest.stasis4j.client

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

    "createAcquisition" in {
      val metadata = SampleData("my_test",
        Acquisition("instrument A", "GCTOF", "positive", "gcms"),
        Metadata("123456", "rat", "tissue"),
        Userdata("file123", ""))

      val res = client.createAcquisition(metadata)
      res.getStatusCode === 200
      logger.info(s"Created metadata '${metadata.sample}")
      logger.info(s"acq response: ${res.getBody.toString}")
    }

    "getAcquisition" in {
      val res = client.getAcquisition("my_test")

      res should not be null
      res.sample should equal("my_test")
      res.acquisition.instrument should equal("instrument A")
      res.metadata.`class` should equal("123456")
      res.userdata.label should equal("file123")
    }

    "createAquisitionFromMinix" in {
      // MX = 297319
      val res = client.createAquisitionFromMinix("http://minix.fiehnlab.ucdavis.edu/rest/export/297319")
      res.getStatusCode === 200

      logger.debug(s"acqMX response: ${res.getBody.toString}")
    }

    "addTracking" in {
      val res = client.addTracking("test", "entered")
      res.getStatusCode === 200

      logger.debug(s"track response: ${res.getBody.toString}")
    }

    "getTracking" in {
      val res = client.getTracking("test")

      res should not be null
      res.id should equal("test")
      res.status should not be empty
      res.status.head.value should equal("ENTERED")
    }

    "addResult" in {
      val data = ResultData("testResult",
        Correction(5, "test",
          Array(Curve(121.12, 121.2),
            Curve(123.12, 123.2))),
        Map[String, Seq[Result]](
          "test_1" -> Array(
            Result(
              Target(121.12, "test", "test_id", 12.2),
              Annotation(121.2, 10.0, replaced = false, 12.2)
            ),
            Result(
              Target(123.12, "test2", "test_id2", 132.12),
              Annotation(123.2, 103.0, replaced = true, 132.12)
            )
          )
        ).asJava
      )

      val res = client.addResult(data)
      res.getStatusCode === 200

      logger.debug(s"result response: ${res.getBody.toString}")
    }

    "getResult" in {
      val res = client.getResults("testResult").body
      logger.info(s"Result response: ${res}")

      res.sample should equal("testResult")
      res.correction should not be null
      res.injections should have size 1
    }

  }
}
