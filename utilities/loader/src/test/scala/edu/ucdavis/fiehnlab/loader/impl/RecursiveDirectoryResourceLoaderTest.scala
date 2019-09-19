package edu.ucdavis.fiehnlab.loader.impl

import java.io.File
import java.util.zip.ZipInputStream

import edu.ucdavis.fiehnlab.loader.TestConfiguration
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
@SpringBootTest(classes = Array(classOf[TestConfiguration]))
class RecursiveDirectoryResourceLoaderTest extends WordSpec {


  @Autowired
  val loader: RecursiveDirectoryResourceLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "RecursiveDirectoryResourceLoaderTest" should {

    "fail loading this resource" in {
      assert(loader.load("/test2.txt").isEmpty)
    }

    "succeed loading this resource" in {
      assert(loader.load("test.txt").isDefined)
    }

    "succeed loading this RecursiveDirectoryResourceLoaderTest.scala" in {
      assert(loader.load("RecursiveDirectoryResourceLoaderTest.scala").isDefined)
    }

    "succeed loading this resource and going to the root" in {
      assert(loader.load("test.txt").isDefined)
    }

    "succeed checking file" in {
      assert(loader.exists("test.txt"))
    }

    "succeed checking file @ subfolder" in {
      assert(loader.exists("test3.txt"))
    }

    "return zip imput stream" ignore { // due to commit 0a2b73f250d3d158037b228bc352b77768a88941
      val result = loader.load("testA.d")

      assert(result.isDefined)
      assert(result.get.isInstanceOf[ZipInputStream])
      result.get.close()
    }
    "return zip imput stream from subfolder recursively" ignore { // due to commit 0a2b73f250d3d158037b228bc352b77768a88941
      val result = loader.load("testB.d")

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
class RecursiveDirectoryResourceLoaderTestConfiguration{

  @Bean
  def recursiveDirectoryLoader:RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("src"))
}
