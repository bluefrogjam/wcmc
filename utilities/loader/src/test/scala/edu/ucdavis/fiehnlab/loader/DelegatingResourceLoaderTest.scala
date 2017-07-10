package edu.ucdavis.fiehnlab.loader

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
class DelegatingResourceLoaderTest extends WordSpec {

  @Autowired
  val loader: DelegatingResourceLoader = null

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "DelegatingResourceLoaderTest" should {

    "has enough loaders defined" in{
      assert(loader.loaders.size() >= 3)
    }

    "fail loading this resource" in {
      assert(loader.load("/test2.txt").isEmpty)
    }

    "succeed loading this resource" in {
      assert(loader.load("/test.txt").isDefined)
    }

    "succeed loading this resource and going to the root" in {
      assert(loader.load("test.txt").isDefined)
    }

  }
}