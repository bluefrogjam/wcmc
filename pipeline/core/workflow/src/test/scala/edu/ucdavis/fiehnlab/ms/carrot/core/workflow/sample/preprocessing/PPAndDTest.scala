package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.preprocessing

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk.MSDKSample
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, TxtStreamLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MSDialProcessedSample
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.{MSDialProcessing, MSDialProcessingProperties}
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration, Profile}
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by diego on 2/7/2018
  **/
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[PPAndDTestConfiguration]))
@ActiveProfiles(Array("back-end", "carrot.processing.peakdetection", "quantify-by-scan"))
class PPAndDTest extends WordSpec with Matchers with LazyLogging {
  @Autowired
  val peakDetection: PeakDetection = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "PeakDetection" should {
    "process mzml sample" in {

      logger.info(new File("../msdial/src/test/resources/testSmall0.mzml").getAbsolutePath)

      val sample = MSDKSample("testSmall0.mzml", new File("../msdial/src/test/resources/testSmall0.mzml"))
      sample.spectra should not be Seq.empty
      sample.spectra should have size 18

      val result = peakDetection.process(sample, AcquisitionMethod(None))
      result should not be Seq.empty
      result shouldBe a[MSDialProcessedSample]

      val deconv = result.asInstanceOf[MSDialProcessedSample]
      deconv.spectra should not be Seq.empty
      deconv.spectra should have size 125

    }
  }
}

@Configuration
@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class PPAndDTestConfiguration {
  @Autowired
  val resourceLoader: DelegatingResourceLoader = null

  @Bean
  def peakDetection: PeakDetection = new PeakDetection()

  @Bean
  def msdialProcessing: MSDialProcessing = new MSDialProcessing()

  @Bean
  def fserv4j: FServ4jClient = new FServ4jClient("testfserv.fiehnlab.ucdavis.edu", 80)

  @Bean
  def targetLibrary: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](resourceLoader.loadAsFile("targets.txt").get, "\t")

  @Bean
  def resourceLoaderSrc: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("src"))

  @Profile(Array("carrot.processing.peakdetection"))
  @Bean
  def msdialProcessingProperties = new MSDialProcessingProperties()
}
