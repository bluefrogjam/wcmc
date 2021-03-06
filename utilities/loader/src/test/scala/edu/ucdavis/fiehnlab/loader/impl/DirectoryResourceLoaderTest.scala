package edu.ucdavis.fiehnlab.loader.impl

import java.io.File
import java.util.zip.ZipInputStream

import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by wohlg on 7/28/2016.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[DirectoryResourceLoaderTestConfiguration]))
class DirectoryResourceLoaderTest extends WordSpec with Logging {

  @Autowired
  val loader: DirectoryResourceLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DirectoryResourceLoaderTest" should {

    "fail loading this resource" in {
      assert(loader.load("/test2.txt").isEmpty)
    }

    "succeed loading this resource" in {
      assert(loader.load("/test.txt").isDefined)
    }

    "succeed loading this resource and going to the root" in {
      assert(loader.load("test.txt").isDefined)
    }

    "succeed finding file" in {
      assert(loader.exists("test.txt"))
    }

    "fail finding file 3" in {
      assert(!loader.exists("test3.txt"))
    }

    "fail finding file @ root" in {
      assert(!loader.exists("/test3.txt"))
    }

    "fail finding missing file" in {
      assert(!loader.exists("sub/test2.txt"))
    }

    "succeed finding file in subdir" in {
      assert(loader.exists("sub/test3.txt"))
    }

    "succeed finding file in subdir from root" in {
      assert(loader.exists("/sub/test3.txt"))
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

@Configuration
class DirectoryResourceLoaderTestConfiguration{

  @Bean
  def directoryLoader:DirectoryResourceLoader = new DirectoryResourceLoader(new File("src/test/resources"))
}
