package edu.ucdavis.fiehnlab.ms.carrot.core.io

import java.io.File

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.loader.{DelegatingResourceLoader, ResourceLoader}
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation._
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlg on 7/28/2016.
  */
@RunWith(classOf[SpringRunner])
@ActiveProfiles(Array("file.source.eclipse", "file.source.luna"))
@SpringBootTest
class ResourceLoaderSampleLoaderTest extends WordSpec with Matchers with Logging {

  @Autowired
  val loader: ResourceLoaderSampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ResourceLoaderSampleLoaderTest" should {


    "able to load d.zip sample GLA_Ag6_Lipids_QC01" in {

      val sample = loader.loadSample("GLA_Ag6_Lipids_QC01.d.zip")

      assert(sample.isDefined)
      assert(sample.get.fileName == "GLA_Ag6_Lipids_QC01.d.zip")
    }

    "able to load mzml sample X-blank_04" in {

      val sample = loader.loadSample("X-blank_04.mzml")

      assert(sample.isDefined)
      assert(sample.get.fileName == "X-blank_04.mzml")
    }

  }
}
