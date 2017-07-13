package edu.ucdavis.fiehnlab.ms.carrot.core.io

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.{CachedResourceLoader, RecursiveDirectoryResourceLoader}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.wcms.utilities.casetojson.config.CaseClassToJSONSerializationConfiguration
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}
import org.springframework.test.context.{ContextConfiguration, TestContextManager}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
	* Created by diego on 7/13/2017.
	*/
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[CachedSampleLoaderTestConfiguration]))
class CachedSampleLoaderTest extends WordSpec with LazyLogging {
	@Autowired
	val loader: CachedSampleLoader = null

	new TestContextManager(this.getClass).prepareTestInstance(this)

	"CachedSampleLoaderTest" should {

		"able to load sample file.msdial" in {
			logger.debug(s"Using loader: ${loader.getClass.getSimpleName}")

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
class CachedSampleLoaderTestConfiguration {

	@Value("${loaders.cached.directory:src/resources}")
	var directory: String = ""

	@Bean
	def resourceLoader: ResourceLoader = new CachedResourceLoader(new File(directory))

	@Bean
	def loader: SampleLoader = new CachedSampleLoader(resourceLoader)
}
