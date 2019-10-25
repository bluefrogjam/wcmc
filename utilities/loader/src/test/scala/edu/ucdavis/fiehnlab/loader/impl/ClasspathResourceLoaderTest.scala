package edu.ucdavis.fiehnlab.loader.impl

import edu.ucdavis.fiehnlab.loader.TestConfiguration
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by wohlg on 7/28/2016.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TestConfiguration]))
class ClasspathResourceLoaderTest extends WordSpec with Matchers with Logging {

  @Autowired
  val loader: ClasspathResourceLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ClasspathResourceLoaderTest" should {

    "fail loading a missing resource" in {
      loader.load("/missing.txt") should not be defined
    }

    "fail loading a missing resource without slash" in {
      loader.load("missing.txt") should not be defined
    }

    "succeed loading this resource" in {
      loader.load("/test.txt") shouldBe defined
    }

    "succeed loading this resource from root" in {
      loader.load("test.txt") shouldBe defined
    }

    "succeed loading this resource as file" in {
      loader.loadAsFile("/test.txt") shouldBe defined
    }

    "succeed loading this resource and going to the root" in {
      loader.load("test.txt") shouldBe defined
    }

    "succeed finding file in subfolder" in {
      loader.exists("sub/test3.txt") shouldBe true
    }

    "succeed finding file in subfolder with slash" in {
      loader.exists("/sub/test3.txt") shouldBe true
    }

    "pass when checking a file with isFile" in {
      loader.isFile("test.txt") shouldBe true
    }

    "fails when checking a file with isDirectory" in {
      loader.isDirectory("test.txt") shouldBe false
    }

    "pass when checking a folder with isDirectory" in {
      loader.isDirectory("testA.d") shouldBe true
    }

    "fails when checking a file with isFile" in {
      loader.isFile("testA.d") shouldBe false
    }

    "load a file in other jar" in {
      val data = loader.load("META-INF/NOTICE.txt").get
      data should not be null
      val buffer = new Array[Byte](data.available())
      data.read(buffer)
      val str: String = buffer.map(_.toChar).mkString("")
      logger.info(s"DATA: ${str}")
      str should startWith("Apache Commons IO")
    }
  }
}
