package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.leco

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.wcmc.api.rest.everything4j.Everything4J
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlg_000 on 5/6/2016.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("file.source.eclipse"))
class LecoSampleTest extends WordSpec with Matchers with BeforeAndAfterEach with Logging {

  @Autowired
  val client: Everything4J = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "LecoSampleTest" should {

    "read sample leco.txt" must {
      val sample = new LecoSample(getClass.getResourceAsStream("/leco.txt"), "leco.txt")

      "able to read all the spectra" in {
        assert(sample.spectra.size == 133)
      }

      "ensure that all spectra have the correct properties" in {
        sample.spectra.foreach { spectra =>

          assert(spectra.associatedScan.get.ions.nonEmpty)
          assert(spectra.retentionTimeInSeconds > 0)

          assert(spectra.associatedScan.get.modelIons.size == 1)
          assert(spectra.purity.get > 0)
          assert(spectra.associatedScan.get.splash() != null)

        }
      }
    }

    "read sample QC6 (2013)_1.txt " must {

      "able to load it" should {
        val sample = new LecoSample(client.load("QC6 (2013)_1.txt").get, "QC6 (2013)_1.txt")

        "able to read all" in {
          logger.debug(sample.spectra.toString())
          assert(sample.spectra.size == 298)
        }
      }


    }
  }

}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class TestConfig {

}
