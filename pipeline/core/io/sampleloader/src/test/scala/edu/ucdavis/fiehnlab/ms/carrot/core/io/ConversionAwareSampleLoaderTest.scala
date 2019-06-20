package edu.ucdavis.fiehnlab.ms.carrot.core.io

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.reflect.io.File

@RunWith(classOf[SpringRunner])
@ActiveProfiles(Array("file.source.eclipse"))
@SpringBootTest(classes = Array(classOf[SampleLoaderTestConfig]))
class ConversionAwareSampleLoaderTest extends WordSpec with Matchers with BeforeAndAfter {

  @Autowired
  val loader: SampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  before({
    File("/tmp/MM8.mzML").delete()
  })

  "ConversionAwareSampleLoaderTest" should {

    s"able to load sample MM8.mzML" in {

      val name = "MM8.mzML"
      val sample = loader.loadSample(name)

      sample shouldBe defined
      sample.get.fileName === name
    }
  }
}
