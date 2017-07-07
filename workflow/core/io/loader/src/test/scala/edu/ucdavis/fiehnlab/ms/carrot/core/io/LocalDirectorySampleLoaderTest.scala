package edu.ucdavis.fiehnlab.ms.carrot.core.io

import com.typesafe.scalalogging.LazyLogging
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.collection.JavaConverters._
/**
  * Created by wohlg on 7/11/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[LocalDirectorySampleLoaderTestTest]))
class LocalDirectorySampleLoaderTest extends WordSpec with LazyLogging {

  @Autowired
  val loader: LocalDirectorySampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "LocalDirectorySampleLoaderTest" should {

    "loadSample in the same directories" must {

      "succeed" in {
        assert(loader.loadSample("file.msdial").isDefined)
      }

      "fail" in {
        assert(loader.loadSample("fileNotFound.msdial").isEmpty)
      }
    }

    "check if samples exist" must {

      "suceed" in {
        assert(loader.sampleExists("file.msdial"))

        assert(loader.sampleExists("test.txt"))
      }

      "fail" in {
        assert(!loader.sampleExists("fileNotFound.msdial"))
      }
    }
  }
}

@Configuration
@ComponentScan
class LocalDirectorySampleLoaderTestTest{

  @Bean
  def localDirectorySampleLoader(properties: LocalDirectorySampleLoaderProperties): LocalDirectorySampleLoader = {
    properties.directories = ("./" :: "src/test/resources" :: "src/test/resources/sub" :: List()).asJava

    new LocalDirectorySampleLoader(properties)
  }

}