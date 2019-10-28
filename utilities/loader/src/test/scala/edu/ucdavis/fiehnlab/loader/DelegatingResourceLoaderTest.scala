package edu.ucdavis.fiehnlab.loader

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
class DelegatingResourceLoaderTest extends WordSpec with Matchers {

  @Autowired
  val loader: DelegatingResourceLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DelegatingResourceLoaderTest" should {

    "has enough loaders defined" in {
      loader.loaders.size should be >= 3
    }

    "fail loading this resource" in {
      loader.load("test2.txt") shouldBe empty
    }

    "succeed loading this resource" in {
      loader.load("test.txt") shouldBe defined
    }

    "succeed loading this resource and going to the root" in {
      loader.load("test.txt") shouldBe defined
    }

    "succeed loading a file with '/' in front" in {
      loader.load("/test.txt") shouldBe defined
    }

  }
}
