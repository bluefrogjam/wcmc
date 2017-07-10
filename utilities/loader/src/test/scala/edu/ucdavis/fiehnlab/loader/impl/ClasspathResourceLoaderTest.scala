package edu.ucdavis.fiehnlab.loader.impl

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.loader.TestConfiguration
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlg on 7/28/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[TestConfiguration]))
class ClasspathResourceLoaderTest extends WordSpec with LazyLogging {

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
  }
}
