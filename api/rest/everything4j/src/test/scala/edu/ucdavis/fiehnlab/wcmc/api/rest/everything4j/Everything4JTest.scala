package edu.ucdavis.fiehnlab.wcmc.api.rest.everything4j

import java.io.{File, InputStream}

import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.reflect.io.Path

/**
  * Created by wohlgemuth on 10/10/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TestConfig]))
@ActiveProfiles(Array("file.source.eclipse"))
class Everything4JTest extends WordSpec with Matchers with BeforeAndAfter with Logging {

  @Autowired
  val everything4J: Everything4J = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  before {
    logger.warn("deleting 'tmp' folder")
    Path("tmp").deleteRecursively()
  }

  "Everything4JTest" should {

    "load a file" in {
      everything4J.load("090309bsesa100_1.cdf").isDefined shouldBe true

      val file = everything4J.loadAsFile("090309bsesa100_1.cdf").get

    }

    "load an case insensitive file. cause windows sucks" in {

      everything4J.load("B2A_TEDDYLipids_Pos_QC006.mzml").isDefined shouldBe true

      val file = everything4J.loadAsFile("B2A_TEDDYLipids_Pos_QC006.mzml").get
    }

    "load a file with spaces" in {
      everything4J.loadAsFile("QC6 (2013)_1.txt").get
    }

    "return None for invalid folder" in {
      everything4J.load("inalid_raw_folder") should not be defined
    }

    "return a stream for valid folder" in {
      val data: Option[InputStream] = everything4J.load("Tube A.d")
      data shouldBe defined
      data.get.available() should be > 1000
    }

    "exists" must {

      "have file " in {
        everything4J.exists("090309bsesa100_1.cdf") shouldBe true
      }

      "support spaces " in {
        everything4J.exists("QC6 (2013)_1.txt") shouldBe true
      }

      "have not file " in {
        everything4J.exists("090309bsesa100_1.cdf.D") shouldBe false
      }


    }

    "downloadFile" must {
      "recurse over folders" in {
        val name = "Tube A.d"
        val folder = s"D:\\oldluna\\instruments\\qtof\\6550\\rawdata\\2015\\Johannes\\HILIC_05282015\\${name}"
        val tmpDest = s"tmp/${name}"
        val result = everything4J.recurse(folder, tmpDest, name)
        logger.info(s"RESULT: $result")

        val file = new File(result.getOrElse(""))
        file should exist

        Path("testRec").deleteRecursively()
      }

      "create a zip" in {
        val stream = everything4J.createZip("Tube A.d", "D:\\oldluna\\instruments\\qtof\\6550\\rawdata\\2015\\Johannes\\HILIC_05282015")
        logger.info(s"Stream size: ${stream.available()}")
        stream.available() should be > 1000
        stream.close()

        new File("tmp/Tube A.d.zip") should exist
      }
    }

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class TestConfig {

}
