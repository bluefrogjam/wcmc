package edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j

import java.io.File

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by diego on 8/31/2017.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest
class DataFormerClientTests extends WordSpec with ShouldMatchers with LazyLogging {
  @Autowired
  val dfClient: DataFormerClient = null

  @LocalServerPort
  val port: Int = 0

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "edu.ucdavis.fiehnlab.wcmc.api.rest.dataform4j.DataFormerClient" should {
    "fail for invalid file" in {
      val filename = "not_found.d.zip"
      val result = dfClient.convert(filename)

      result should contain key "error"
      result("error") shouldEqual (s"File ${filename} doesn't exist")
    }

    "convert a raw data file (.d.zip)" in {
      val mapper = new ObjectMapper
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
@Import(Array(classOf[MonitorConfig]))
class DataFormerClientTestConfiguration {
  @Bean
  def dfClient: DataFormerClient = new DataFormerClient()
}
