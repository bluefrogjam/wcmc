package edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.api.rest.fserv4j.FServ4jClient
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by diego on 8/31/2017.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[DataFormerClientTestConfiguration]))
class DataFormerClientTests extends WordSpec with ShouldMatchers with LazyLogging {
  @Autowired
  val dfClient: DataFormerClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient" should {
    "fail for invalid .d.zip file" in {
      val filename = "not_found.d.zip"
      val result = dfClient.convert(filename)

      result should contain key "error"
      result("error") shouldEqual (s"File ${filename} doesn't exist")
    }

    "convert a raw data file (.d.zip)" in {
      //      val mapper = new ObjectMapper
      val filename = "B5_P20Lipids_Pos_QC000.d.zip"

      val result = dfClient.convert(filename)

      result should contain key "abf"
      result should contain key "mzml"

      val fileNoExt = filename.substring(0, filename.indexOf("."))

      result("abf") should equal(s"${fileNoExt}.abf")
      result("mzml") should equal(s"${fileNoExt}.mzml")

      Array(".abf", ".d.zip", ".mzml").foreach(s => new File(fileNoExt + s).deleteOnExit())

    }

  }
}

@Configuration
@Import(Array(classOf[DataFormerConfiguration]))
class DataFormerClientTestConfiguration {
  @Value("${wcmc.api.rest.fserv4j.host:testfserv.fiehnlab.ucdavis.edu}")
  val fservHost = ""

  @Value("${wcmc.api.rest.fserv4j.port:80}")
  val fservPort = 0


  @Bean
  def fserv4j: FServ4jClient = new FServ4jClient(fservHost, fservPort)

  @Bean
  def dfClient: DataFormerClient = new DataFormerClient()
}
