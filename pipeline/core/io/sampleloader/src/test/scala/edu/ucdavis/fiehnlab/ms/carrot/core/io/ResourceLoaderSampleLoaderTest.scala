package edu.ucdavis.fiehnlab.ms.carrot.core.io

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.loader.{DelegatingResourceLoader, ResourceLoader}
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
@ActiveProfiles(Array("file.source.eclipse", "file.source.luna"))
@ContextConfiguration(classes = Array(classOf[ResourceLoaderSampleLoaderTestConfiguration]))
class ResourceLoaderSampleLoaderTest extends WordSpec with ShouldMatchers with LazyLogging {

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

@Configuration
@ComponentScan(basePackageClasses = Array(classOf[ResourceLoaderSampleLoaderTestConfiguration],classOf[DelegatingResourceLoader]))
@Import(Array(classOf[CaseClassToJSONSerializationAutoConfiguration]))
class ResourceLoaderSampleLoaderTestConfiguration {

  @Bean
  def resourceLoader: ResourceLoader = new RecursiveDirectoryResourceLoader(new File("src"))

  @Bean
  def loader(delegatingResourceLoader: DelegatingResourceLoader): ResourceLoaderSampleLoader = new ResourceLoaderSampleLoader(delegatingResourceLoader)
}
