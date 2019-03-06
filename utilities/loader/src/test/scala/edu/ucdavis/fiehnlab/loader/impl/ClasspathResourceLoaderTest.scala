package edu.ucdavis.fiehnlab.loader.impl

import java.io.InputStream
import java.util.zip.ZipInputStream

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.loader.TestConfiguration
import org.junit.{After, Before}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlg on 7/28/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TestConfiguration]))
class ClasspathResourceLoaderTest extends WordSpec with Logging with BeforeAndAfter {

  @Autowired
  val loader: ClasspathResourceLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ClasspathResourceLoaderTest" should {

    "fail loading this resource" in {
      assert(loader.load("/test2.txt").isEmpty)
    }

    "succeed loading this resource" in {
      assert(loader.load("/test.txt").isDefined)
    }

    "succeed loading this resource as file" in {
      assert(loader.loadAsFile("/test.txt").isDefined)
    }

    "succeed loading this resource and going to the root" in {
      assert(loader.load("test.txt").isDefined)
    }

    "succeed finding file in subfolder" in {
      assert(loader.exists("sub/test3.txt"))
    }
    "succeed finding file in subfolder from root" in {
      assert(loader.exists("/sub/test3.txt"))
    }
    "fail finding file @ root" in {
      assert(!loader.exists("/test3.txt"))
    }
    "fail finding file without subfolder" in {
      assert(!loader.exists("test3.txt"))
    }

    "return zip imput stream" in {
      val result = loader.load("/testA.d")

      assert(result.isDefined)
      assert(result.get.isInstanceOf[ZipInputStream])
      result.get.close()
    }
    "return zip imput stream from subfolder" in {
      val result = loader.load("sub/testB.d")

      assert(result.isDefined)
      assert(result.get.isInstanceOf[ZipInputStream])
      result.get.close()
    }

    "pass when checking a file with isFile" in {
      assert(loader.isFile("test.txt"))
    }
    "fails when checking a file with isDirectory" in {
      assert(!loader.isDirectory("test.txt"))
    }

    "pass when checking a folder with isDirectory" in {
      assert(loader.isDirectory("testA.d"))
    }
    "fails when checking a file with isFile" in {
      assert(!loader.isFile("testA.d"))
    }

  }
}
