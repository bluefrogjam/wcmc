package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.TargetedWorkflowTestConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MSDialGCMSProcessedSample
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.{MSDialProcessing, MSDialProcessingProperties}
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by sajjan on 4/18/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TargetedWorkflowTestConfiguration]))
@ActiveProfiles(Array("carrot.gcms", "file.source.eclipse"))
class MSDialGCMSProcessingTest extends WordSpec with Matchers with LazyLogging {

  @Autowired
  val msdProcessing: MSDialProcessing = null

  @Autowired
  val sampleLoader: SampleLoader = null

  @Autowired
  val properties: MSDialProcessingProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MSDialGCMSProcessingTest" should {
    "check peakpicking" in {
      val sample = sampleLoader.getSample("180321bZKsa26_1.cdf")
      sample.spectra.size should be > 1

      val outSample = msdProcessing.process(sample, properties)

      logger.debug(s"Sample result: $outSample")
      outSample.spectra.foreach(x => println(x))
      outSample.spectra should not be null
      outSample.spectra.size should be > 500

      outSample shouldBe a[MSDialGCMSProcessedSample]
    }
  }
}