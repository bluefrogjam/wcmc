package edu.ucdavis.fiehnlab.ms.carrot.core.io

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.loader.{DelegatingResourceLoader, ResourceLoader}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.msdk.MSDKSample
import edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config.CaseClassToJSONSerializationAutoConfiguration
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation._
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, ContextConfiguration, TestContextManager}

/**
  * Created by wohlg on 7/28/2016.
  */
@RunWith(classOf[SpringRunner])
@ActiveProfiles(Array("file.source.eclipse"))
@ContextConfiguration(classes = Array(classOf[ResourceLoaderSampleLoaderTestConfiguration]))
class ResourceLoaderSampleLoaderTest extends WordSpec with ShouldMatchers with LazyLogging {

  @Autowired
  val loader: ResourceLoaderSampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ResourceLoaderSampleLoaderTest" should {


    "able to load sample Prerun_NoInj001.d.zip" in {

      val sample = loader.loadSample("Prerun_NoInj001.d.zip")

      assert(sample.isDefined)
      assert(sample.get.fileName == "Prerun_NoInj001.d.zip")
    }

    "able to read a .d file with spaces" ignore { //TODO: trace the sample loading
      val name = "Tube A.d"
      val sample = loader.loadSample(name)

      sample.isDefined shouldBe true
      sample.get.fileName === name
      sample.get.spectra.length should be > 10
    }

    "able to read a .d file without spaces" ignore { //TODO: trace the sample loading
      val name = "0-up.d"
      val sample = loader.loadSample(name)

      sample.isDefined shouldBe true
      sample.get.fileName === name
      sample.get shouldBe a[MSDKSample]
      sample.get.spectra.length should be > 10
    }

  }
}

@Configuration
@ComponentScan(basePackageClasses = Array(classOf[ResourceLoaderSampleLoaderTestConfiguration],classOf[DelegatingResourceLoader]))
@Import(Array(classOf[CaseClassToJSONSerializationAutoConfiguration]))
class ResourceLoaderSampleLoaderTestConfiguration {

  @Bean
  def resourceLoader: ResourceLoader = new RecursiveDirectoryResourceLoader(new File("src"))

  @Bean
  def loader(delegatingResourceLoader: DelegatingResourceLoader): ResourceLoaderSampleLoader = new ResourceLoaderSampleLoader(delegatingResourceLoader)
}
