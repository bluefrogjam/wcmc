package edu.ucdavis.fiehnlab.ms.carrot.core.msdial

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by diego on 3/6/2019
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest()
@ActiveProfiles(Array("carrot.lcms", "file.source.eclipse", "test"))
class QExactiveProcessingTest extends WordSpec with Logging with Matchers {

  @Autowired
  val msdProcessing: MSDialLCMSProcessing = null

  @Autowired
  val properties: MSDialLCMSProcessingProperties = null

  @Autowired
  val sampleLoader: SampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MSDialProcessing" should {
    val sample = sampleLoader.loadSample("Biorec002_posCSH_postFlenniken010.mzml")
    "have defined sample" in {
      sample shouldBe defined
    }

    "find MS and MSMS spectra" in {
      properties.ionMode = PositiveMode()

      val deconvoluted = msdProcessing.process(sample.get, properties)

      deconvoluted.spectra.count(_.associatedScan.get.msLevel == 2) should be > 0
    }
  }
}
