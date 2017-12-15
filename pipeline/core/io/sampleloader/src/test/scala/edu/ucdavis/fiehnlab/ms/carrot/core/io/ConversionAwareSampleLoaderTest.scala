package edu.ucdavis.fiehnlab.ms.carrot.core.io

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient
import edu.ucdavis.fiehnlab.wcmc.api.rest.everything4j.Everything4JAutoConfiguration
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest
class ConversionAwareSampleLoaderTest extends WordSpec {

  @Autowired
  val loader: SampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ConversionAwareSampleLoaderTest" should {

    "able to load sample B5_P20Lipids_Pos_NIST02" in {

      val sample = loader.loadSample("B5_P20Lipids_Pos_NIST02.mzXML")

      assert(sample.isDefined)
      assert(sample.get.fileName == "B5_P20Lipids_Pos_NIST02.mzXML")
    }

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration], classOf[Everything4JAutoConfiguration]))
class ConversionAwareSampleLoaderTestConfiguration {

  @Autowired
  val dataForm: DataFormerClient = null

  @Bean
  def loader: ConversionAwareSampleLoader = new ConversionAwareSampleLoader(dataForm, client)

  @Bean
  def client: FServ4jClient = new FServ4jClient(
    "testfserv.fiehnlab.ucdavis.edu",
    80
  )
}