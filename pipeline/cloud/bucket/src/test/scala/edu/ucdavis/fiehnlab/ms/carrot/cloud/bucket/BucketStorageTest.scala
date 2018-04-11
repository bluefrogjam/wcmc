package edu.ucdavis.fiehnlab.ms.carrot.cloud.bucket

import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class BucketStorageTestApplication

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest
@ActiveProfiles(Array("carrot.resource.store.bucket"))
class BucketStorageTest extends WordSpec {

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "BucketStorageTest" should {

    "store" in {

    }

  }
}
