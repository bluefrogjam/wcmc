package edu.ucdavis.fiehnlab.ms.carrot.core.io

import java.io.File

import edu.ucdavis.fiehnlab.loader.{DelegatingResourceLoader, ResourceLoader}
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config.CaseClassToJSONSerializationAutoConfiguration
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation._
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


    "able to load sample B5_P20Lipids_Pos_NIST02" in {

      val sample = loader.loadSample("B5_P20Lipids_Pos_NIST02.mzML")

      assert(sample.isDefined)
      assert(sample.get.fileName == "B5_P20Lipids_Pos_NIST02.mzML")
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

  @Bean
  def client:FServ4jClient = new FServ4jClient(
    "testfserv.fiehnlab.ucdavis.edu",
    80
  )
}
