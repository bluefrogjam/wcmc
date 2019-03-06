package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.abf

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSMSSpectra
import edu.ucdavis.fiehnlab.wcmc.api.rest.everything4j.Everything4JAutoConfiguration
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import edu.ucdavis.fiehnlab.wcmc.api.rest.msdialrest4j.MSDialRestProcessor
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by wohlgemuth on 7/12/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest()
class ABFSampleTest extends WordSpec with Matchers with Logging {

  @Autowired
  val client: MSDialRestProcessor = null

  @Autowired
  val loader:FServ4jClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ABFSampleTest" should {

    "sblank sample" in {
      val name  = "B5_P20Lipids_Pos_Blank000.abf"
      val sample = new ABFSample(name,loader.loadAsFile(name).get,client)

      sample.spectra should not be empty
    }

    "qc sample" in {
      val name  = "B5_P20Lipids_Pos_QC000.abf"
      val sample = new ABFSample(name,loader.loadAsFile(name).get,client)

      sample.spectra should not be empty
    }


    "msms sample" in {
      val name  = "B5_SA0262_P20Lipids_Pos_1FV_2404_MSMS.abf"
      val sample = new ABFSample(name,loader.loadAsFile(name).get,client)

      sample.spectra should not be empty

      val msms = sample.spectra.collect{
        case x:MSMSSpectra => x
      }

      msms should not be empty
    }



  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration],classOf[Everything4JAutoConfiguration]))
class ABFSampleTestConfig {
  @Bean
  def client:FServ4jClient = new FServ4jClient(
    "testfserv.fiehnlab.ucdavis.edu",
    80
  )
}
