package edu.ucdavis.fiehnlab.ms.carrot.core.io

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import scala.collection.JavaConverters._

@RunWith(classOf[SpringRunner])
@ActiveProfiles(Array("file.source.eclipse"))
@SpringBootTest(classes = Array(classOf[SampleLoaderTestConfig]))
class ConversionAwareSampleLoaderTest extends WordSpec with Matchers {

  @Autowired
  val loader: SampleLoader = null

  @Autowired
  val cacheManager: CacheManager = null
  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ConversionAwareSampleLoaderTest" should {


    s"able to load sample MM8.mzML" in {

      val name = "MM8.mzML"
      val sample = loader.getSample(name)
      sample.fileName === name
      val result = cacheManager.getCache("resource-get-sample").get("MM8.mzML").get().asInstanceOf[Sample]

      assert(result != null)
    }
  }
}
