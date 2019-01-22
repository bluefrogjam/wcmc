package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.agilent

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient
import edu.ucdavis.fiehnlab.wcmc.api.rest.everything4j.Everything4JAutoConfiguration
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

@RunWith(classOf[SpringRunner])
@SpringBootTest()
class AgilentSampleTest extends WordSpec with Logging with ShouldMatchers{


  @Autowired
  val loader: FServ4jClient = null

  @Autowired
  val dataFormerClient: DataFormerClient = null

  val name: String = "B5_P20Lipids_Pos_QC029.d.zip"

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "AgilentSampleTest" should {

    "spectra" in {

      val sample = new AgilentSample(name, loader.loadAsFile(name).get, dataFormerClient)

      logger.info(s"spectra: ${sample.spectra.size}")

      sample.spectra.size should be > 0
    }

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration], classOf[Everything4JAutoConfiguration]))
class ABFSampleTestConfig {
  @Bean
  def client: FServ4jClient = new FServ4jClient(
    "testfserv.fiehnlab.ucdavis.edu",
    80
  )
}
