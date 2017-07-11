package edu.ucdavis.fiehnlab.ms.carrot.core.io

import java.io.File

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.wcms.utilities.casetojson.config.CaseClassToJSONSerializationConfiguration
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

/**
  * Created by wohlg on 7/28/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[ResourceLoaderSampleLoaderTestConfiguration]))
class ResourceLoaderSampleLoaderTest extends WordSpec {

  @Autowired
  val loader: ResourceLoaderSampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ResourceLoaderSampleLoaderTest" should {

    "able to load sample file.msdial" in {

      val sample = loader.loadSample("file.msdial")

      assert(sample.isDefined)
      assert(sample.get.fileName == "file.msdial")
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
@ComponentScan(basePackages = Array("edu.ucdavis.fiehnlab.wcms.api.rest.msdialrest4j"))
@Import(Array(classOf[CaseClassToJSONSerializationConfiguration]))
class ResourceLoaderSampleLoaderTestConfiguration {

  @Bean
  def resourceLoader: ResourceLoader = new RecursiveDirectoryResourceLoader(new File("src"))

  @Bean
  def loader: ResourceLoaderSampleLoader = new ResourceLoaderSampleLoader(resourceLoader)
}
