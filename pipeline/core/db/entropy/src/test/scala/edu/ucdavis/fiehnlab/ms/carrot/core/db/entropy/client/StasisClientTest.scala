package edu.ucdavis.fiehnlab.ms.carrot.core.db.entropy.client

import com.typesafe.scalalogging.LazyLogging
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

@RunWith(classOf[SpringRunner])
@SpringBootTest
class StasisClientTest extends WordSpec with ShouldMatchers with LazyLogging {

  @Autowired
  val client: StasisClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "StasisClientTest" should {

    "createAcquisition" in {

    }

    "createAquisitionFromMinix" in {

    }

    "addResult" in {

    }

    "addTracking" in {

    }

    "getAcquisition" in {
      /*
      {"sample": "180415dZKsa20_1", "acquisition": {"instrument": "Leco GC-Tof", "name": "GCTOF", "
        ""ionisation": "positive", "method": "gcms"}, "metadata": {"class": "382172", "species": "
        ""rat", "organ": "tissue"}, "userdata": {"label": "GP_S_6_006", "comment": ""}, "
        ""time": 1525121375499, "id": "180415dZKsa20_1"}
       */
      val acqData = client.getAcquisition("180415dZKsa20_1")

      acqData.sample should equal("180415dZKsa20_1")
      acqData.acquisition.instrument should equal("Leco GC-Tof")
      acqData.metadata.clazz should equal("382172")
      acqData.userdata.label should equal("GP_S_6_006")
    }

    "getResults" in {

    }

    "getTracking" in {

    }

  }
}
