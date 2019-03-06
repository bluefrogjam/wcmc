package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk

import java.io.File

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

@RunWith(classOf[SpringRunner])
@SpringBootTest
class BugWCMC7Test extends WordSpec with Matchers with Logging {

  @Autowired
  val resourceLoader: ResourceLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MSDKSample delegate" should {

    logger.warn("FOR THESE TO RUN YOU NEED THE REQUIRED FILES UNDER ~/.carrot_storage")

    "load small mzml" ignore {
      val file = resourceLoader.loadAsFile("B1_P20Lipids_Pos_Blank000_pp_zs.mzML")
      assert(file.isDefined)

      val sample: MSDKSample = MSDKSample("B1_P20Lipids_Pos_Blank000_pp_zs.mzML", file.get)

      assert(sample.spectra.size > 1)
    }
    "load 32bit float mzml" ignore {
      val file = resourceLoader.loadAsFile("B1_P20Lipids_Pos_Blank000_x86.mzML")
      assert(file.isDefined)

      val sample: MSDKSample = MSDKSample("B1_P20Lipids_Pos_Blank000.mzML", file.get)

      assert(sample.spectra.size > 1)
    }
    "load zero sample filtered mzml" ignore {
      val file = resourceLoader.loadAsFile("B1_P20Lipids_Pos_Blank000_zs.mzML")
      assert(file.isDefined)

      val sample: MSDKSample = MSDKSample("B1_P20Lipids_Pos_Blank000.mzML", file.get)

      assert(sample.spectra.size > 1)
    }
    "load large mzml" ignore {
      val file = resourceLoader.loadAsFile("B1_P20Lipids_Pos_Blank000.mzML")
      assert(file.isDefined)

      val sample: MSDKSample = MSDKSample("B1_P20Lipids_Pos_Blank000.mzML", file.get)

      assert(sample.spectra.size > 1)
    }
  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class MSDKTestConfiguration {
  @Bean
  def resourceLoader: ResourceLoader = new RecursiveDirectoryResourceLoader(new File(s"${System.getenv("HOME")}/.carrot_storage/tmp"))
}
