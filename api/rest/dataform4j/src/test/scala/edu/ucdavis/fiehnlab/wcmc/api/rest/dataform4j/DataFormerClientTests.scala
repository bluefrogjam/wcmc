package edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j

import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by diego on 8/31/2017.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[DataFormerClientTestConfiguration]))
class DataFormerClientTests extends WordSpec with Matchers with Logging {
  @Autowired
  val dfClient: DataFormerClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient" should {
    "fail for invalid .d.zip file" in {
      val filename = "not_found.d.zip"

      val result = dfClient.convert(filename)

      result shouldBe None
    }

    // ignoring this since we are not currently scheduling .d.zip files, only mzml
    "convert a raw data file (.d.zip) to mzml" ignore {
      val filename = "testA.d.zip"

      val result = dfClient.convert(filename)

      result.isDefined shouldBe true

      result.get.getName.toLowerCase.endsWith("mzml") shouldBe true
    }


  }
}

@Configuration
@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class DataFormerClientTestConfiguration {
  @Value("${wcmc.api.rest.fserv4j.host:testfserv.fiehnlab.ucdavis.edu}")
  val fservHost = ""

  @Value("${wcmc.api.rest.fserv4j.port:80}")
  val fservPort = 0

  @Bean
  def fserv4j: FServ4jClient = new FServ4jClient(fservHost, fservPort)

}
