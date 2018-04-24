package edu.ucdavis.fiehnlab.ms.carrot.core.io

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient
import edu.ucdavis.fiehnlab.wcmc.api.rest.everything4j.Everything4J
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Profile}
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("file.source.luna"))
class ConversionAwareSampleLoaderTest extends WordSpec with ShouldMatchers {

  @Autowired
  val loader: SampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ConversionAwareSampleLoaderTest" should {

    s"able to load sample MM8.mzML" in {

      val name = "MM8.mzML"
      val sample = loader.loadSample(name)

      sample.isDefined shouldBe true
      sample.get.fileName === name
    }
  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class ConversionAwareSampleLoaderTestConfiguration {

  @Autowired
  val dataForm: DataFormerClient = null

  @Bean
  def loader(resourceLoader:ResourceLoader): ConversionAwareSampleLoader = new ConversionAwareSampleLoader(dataForm, resourceLoader)

}
