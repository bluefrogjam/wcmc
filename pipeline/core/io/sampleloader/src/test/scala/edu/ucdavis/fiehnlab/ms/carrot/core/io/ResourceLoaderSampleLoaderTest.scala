package edu.ucdavis.fiehnlab.ms.carrot.core.io

import java.io.File

import edu.ucdavis.fiehnlab.loader.{RemoteLoader, ResourceLoader}
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j.{CachedMSDialRestProcesser, MSDialRestProcessor}
import edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config.CaseClassToJSONSerializationConfiguration
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

/**
  * Created by wohlg on 7/28/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[ResourceLoaderSampleLoaderTestConfiguration]))
class ResourceLoaderSampleLoaderTest extends WordSpec {
	@Value("${loaders.recursive.basefolder:src/test/resources}")
	val directory: String = null

//	@Value("${wcmc.api.rest.msdialrest4j.port:80}")
//	val port: Int = 80

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
@ComponentScan
@Import(Array(classOf[CaseClassToJSONSerializationConfiguration]))
class ResourceLoaderSampleLoaderTestConfiguration {
	@Value("${loaders.recursive.basefolder:src/test/resources}")
	val directory: String = null

  @Bean
  def resourceLoader: ResourceLoader = new RecursiveDirectoryResourceLoader(new File(directory))

  @Bean
  def loader: ResourceLoaderSampleLoader = new ResourceLoaderSampleLoader(resourceLoader)

	@Bean
	def msdialrest4jClient: MSDialRestProcessor = new CachedMSDialRestProcesser()

	@Bean
	def fServ4jClient: FServ4jClient = new FServ4jClient
}
