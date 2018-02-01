package edu.ucdavis.fiehnlab.ms.carrot.core.msdial

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk.MSDKSample
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by diego on 1/30/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[Config]))
class MSDialProcessingTest extends WordSpec with Matchers with LazyLogging {
  @Autowired
  val msdProcessing: MSDialProcessing = null
  @Autowired
  val properties: MSDialProcessingProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MSDialProcessingTest" should {
    val sample: MSDKSample = MSDKSample("testA.mzml", new File("src/test/resources/testA.mzml"))

    "check peakpicking " in {

      sample.spectra.size should be > 1

      val outSample = msdProcessing.process(sample, properties)
      logger.debug(s"Sample result: ${outSample}")
      outSample.spectra should not be null
      outSample.spectra.size should be > 0
    }

    "check peakpicking in RT range [10.00, 10.44)" ignore {
      properties.retentionTimeBegin = 10.00
      properties.retentionTimeEnd = 10.44

      val outSample = msdProcessing.process(sample, properties)

      outSample.spectra.size should be > 0
    }

    "check peakpicking in RT range [5.01, 5.27)" ignore {
      properties.retentionTimeBegin = 5.01
      properties.retentionTimeEnd = 5.27

      val outSample = msdProcessing.process(sample, properties)

      outSample.spectra.size should be > 0
    }
  }
}

@Configuration
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
class Config {
  @Bean
  def msdProcessing: MSDialProcessing = new MSDialProcessing()

  @Bean
  def properties: MSDialProcessingProperties = new MSDialProcessingProperties()

}
