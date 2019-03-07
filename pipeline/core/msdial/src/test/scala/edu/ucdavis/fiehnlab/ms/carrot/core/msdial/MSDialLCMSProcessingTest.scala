package edu.ucdavis.fiehnlab.ms.carrot.core.msdial

import java.io.File

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk.MSDKSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MSDialLCMSProcessedSample
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.SampleSerializer
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by diego on 1/30/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest()
@ActiveProfiles(Array("carrot.lcms"))
class MSDialLCMSProcessingTest extends WordSpec with Matchers with Logging {

  @Autowired
  val msdProcessing: MSDialLCMSProcessing = null

  @Autowired
  val properties: MSDialLCMSProcessingProperties = null

  @Autowired(required = false)
  val serializer: SampleSerializer = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MSDialLCMSProcessingTest" should {
    // Setting required processing properties
    properties.ionMode = PositiveMode()

    "check peakpicking" in {
      val sample: MSDKSample = MSDKSample("testA.mzml", new File(getClass.getResource("/testA.mzml").getFile))


      sample.spectra.size should be > 1

      val outSample = msdProcessing.process(sample, properties)

      //      logger.debug(s"Sample result: $outSample")
      outSample.spectra should not be null
      outSample.spectra.size should be > 0

      outSample shouldBe a[MSDialLCMSProcessedSample]

      if (serializer != null)
        serializer.saveFile(outSample)
    }

    "check peakpicking in RT range (1.45 - 1.60)" ignore {
      val sample: MSDKSample = MSDKSample("testSmall0.mzml", new File(getClass.getResource("/testSmall0.mzml").getFile))

      val outSample = msdProcessing.process(sample, properties)

      outSample.spectra.size should be > 0
      outSample shouldBe a[MSDialLCMSProcessedSample]

      if (serializer != null) {
        serializer.saveFile(outSample.asInstanceOf[MSDialLCMSProcessedSample])
      }
    }

    "check peakpicking in RT range (10.00 - 10.44)" ignore {
      val sample: MSDKSample = MSDKSample("testSmall1.mzml", new File(getClass.getResource("/testSmall1.mzml").getFile))

      val outSample = msdProcessing.process(sample, properties)

      outSample.spectra.size should be > 0
      outSample shouldBe a[MSDialLCMSProcessedSample]

      if (serializer != null) {
        serializer.saveFile(outSample.asInstanceOf[MSDialLCMSProcessedSample])
      }
    }

    "check peakpicking in RT range (4.74 - 5.50)" ignore {
      val sample: MSDKSample = MSDKSample("testSmall2.mzml", new File(getClass.getResource("/testSmall2.mzml").getFile))

      val outSample = msdProcessing.process(sample, properties)

      outSample.spectra.size should be > 0
      outSample shouldBe a[MSDialLCMSProcessedSample]

      val ions = outSample.spectra.head.associatedScan.get.ions
      ions(0).mass should not be ions(0).intensity

      if (serializer != null) {
        serializer.saveFile(outSample.asInstanceOf[MSDialLCMSProcessedSample])
      }
    }
  }
}
