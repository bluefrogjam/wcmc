package edu.ucdavis.fiehnlab.loader.impl

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.TestConfiguration
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlg on 7/28/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TestConfiguration]))
class DirectoryResourceLoaderTest extends WordSpec with LazyLogging {

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

  }
}

@Configuration
class DirectoryResourceLoaderTestConfiguration{

  @Bean
  def directoryLoader:DirectoryResourceLoader = new DirectoryResourceLoader(new File("src/test/resources"))
}