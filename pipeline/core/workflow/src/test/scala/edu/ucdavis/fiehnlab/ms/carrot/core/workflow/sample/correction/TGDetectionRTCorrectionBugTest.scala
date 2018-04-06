package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, SampleLoader, TxtStreamLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.MSDialProcessingProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.lcms.LCMSTargetRetentionIndexCorrectionProcess
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration, Primary, Profile}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlgemuth on 6/17/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TGTestConfiguration]))
@ActiveProfiles(Array("backend-txt", "quantify-by-scan"))
class TGDetectionRTCorrectionBugTest extends WordSpec with LazyLogging {

  @Autowired
  val correction: LCMSTargetRetentionIndexCorrectionProcess = null

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val targetLibrary: LibraryAccess[Target] = null


  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "LCMSRetentionIndexCorrectionTest" should {

    val sample5 = loader.getSample("w5_posCSH_039.d.zip")
    val sample6 = loader.getSample("w6_posCSH_128.d.zip")
    val method = AcquisitionMethod(None)
    assert(correction != null)


    s"should find Na & NH4 adducts at 11.10+-0.01 in ${sample5}" in {

      val result = correction.process(sample5, method)

      for (x <- result.featuresUsedForCorrection) {
        logger.info(s"used for correction: ${x}")
      }
    }

    s"should find Na & NH4 adducts at 11.10+-0.01 in ${sample6}" in {

      val result = correction.process(sample6, method)

      for (x <- result.featuresUsedForCorrection) {
        logger.info(s"used for correction: ${x}")
      }

    }

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Configuration
class TGTestConfiguration extends LazyLogging {

  @Autowired
  val resourceLoader: DelegatingResourceLoader = null


  /**
    * below there will be all different directory loaders from the different workstations we are working on
    * smarter would be to use spring profiles
    *
    * @return
    */
  @Bean
  def resourceLoaderSrc: RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("/storage"))

  /**
    * our defined library of library targets
    *
    * @return
    */
  @Profile(Array("backend-txt"))
  @Primary
  @Bean
  def targetLibrary: LibraryAccess[Target] = new TxtStreamLibraryAccess[Target](resourceLoader.loadAsFile("targets_20180315.txt").get, "\t")


  @Primary
  @Bean
  def fserv4j: FServ4jClient = new FServ4jClient(
    "testfserv.fiehnlab.ucdavis.edu",
    80
  )

  @Bean
  def msdialProps: MSDialProcessingProperties = new MSDialProcessingProperties()
}
