package edu.ucdavis.fiehnlab.wcmc.api.rest.everything4j

import java.io.{File, FileOutputStream}
import java.nio.file.Files
import java.security.MessageDigest

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.{FileUtils, IOUtils}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterEach, ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by wohlgemuth on 10/10/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TestConfig]))
class Everything4JTest extends WordSpec  with ShouldMatchers with BeforeAndAfterEach with LazyLogging{

  @Autowired
  val everything4J:Everything4J = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Everything4JTest" should {

    "load a file" in {
      everything4J.load("090309bsesa100_1.cdf").isDefined shouldBe true

      val file = everything4J.load("090309bsesa100_1.cdf").get
    }


    "load a folder" in {
      val name = "Tube A.d"
      val testFolder = everything4J.load(name)
      testFolder.isDefined shouldBe true
      testFolder.get.available() should be > 100

      val tmp = new File(s"tmp/${name}.zip")
      val down = new File(s"tmp/donloaded-${name}.zip")
      val downOS = new FileOutputStream(down)

      IOUtils.copyLarge(testFolder.get, downOS)
      downOS.flush()
      downOS.close()
      testFolder.get.close()

      val check1 = MessageDigest.getInstance("SHA-256") digest Files.readAllBytes(tmp.toPath)
      logger.info(s"Checksum 1: ${check1.mkString}")
      val check2 = MessageDigest.getInstance("SHA-256") digest Files.readAllBytes(down.toPath)
      logger.info(s"Checksum 2: ${check2.mkString}")
      check1 should equal(check2)

      FileUtils.deleteDirectory(new File("tmp"))
    }

    "exists" must {

      "have file " in {
        everything4J.exists("090309bsesa100_1.cdf") shouldBe true
      }

      "have not file " in {
        everything4J.exists("090309bsesa100_1.cdf.D") shouldBe false
      }


    }

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class TestConfig{

}
