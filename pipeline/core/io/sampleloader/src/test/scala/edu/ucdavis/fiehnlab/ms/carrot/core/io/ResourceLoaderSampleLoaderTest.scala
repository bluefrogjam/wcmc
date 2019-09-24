package edu.ucdavis.fiehnlab.ms.carrot.core.io

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlg on 7/28/2016.
  */
@RunWith(classOf[SpringRunner])
@ActiveProfiles(Array("file.source.eclipse"))
@SpringBootTest
class ResourceLoaderSampleLoaderTest extends WordSpec with Matchers with Logging {

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val cacheManager: CacheManager = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ResourceLoaderSampleLoaderTest" should {

    "clear cache" in {
      cacheManager.getCache("resource-load-sample").clear()

      assert(cacheManager.getCache("resource-load-sample").get("GLA_Ag6_Lipids_QC01.d.zip") == null)
    }

    "able to load d.zip sample GLA_Ag6_Lipids_QC01" in {

      val sample = loader.getSample("GLA_Ag6_Lipids_QC01.d.zip")

      assert(sample.fileName == "GLA_Ag6_Lipids_QC01.d.zip")
      assert(sample.spectra != null)

    }

    "ensure that object is now cached" in {

      val sample: Sample = cacheManager.getCache("resource-get-sample").get("GLA_Ag6_Lipids_QC01.d.zip").get().asInstanceOf[Sample]

      assert(sample.fileName == "GLA_Ag6_Lipids_QC01.d.zip")
      assert(sample.spectra != null)
    }


    "able to reload d.zip sample GLA_Ag6_Lipids_QC01, which should be cached now" in {

      val sample = loader.getSample("GLA_Ag6_Lipids_QC01.d.zip")

      assert(sample.fileName == "GLA_Ag6_Lipids_QC01.d.zip")
      assert(sample.spectra != null)
    }
    "able to load mzml sample X-blank_04" in {

      val sample = loader.getSample("X-blank_04.mzml")

      assert(sample.fileName == "X-blank_04.mzml")
      assert(sample.spectra != null)
      assert(sample.spectra.nonEmpty)
    }

  }
}
