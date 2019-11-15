package edu.ucdavis.fiehnlab.ms.carrot.cloud.bucket

import java.io.{File, RandomAccessFile}

import edu.ucdavis.fiehnlab.loader.{ResourceLoader, ResourceStorage}
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class BucketStorageTestApplication

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("carrot.resource.store.bucket", "carrot.resource.loader.bucket"))
class BucketStorageTest extends WordSpec with Matchers {

  @Autowired
  val storage: ResourceStorage = null

  @Autowired
  val loader: ResourceLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Bucket data access classes" should {
    "use data bucket" in {
      loader.asInstanceOf[BucketLoader].getBucketName should equal("datatest-carrot")
      storage.asInstanceOf[BucketStorage].getBucketName should equal("datatest-carrot")
    }

    "store" in {

      for (x <- 1 to 10 by 3) {
        val temp = File.createTempFile("temp", "x")
        temp.deleteOnExit()

        val f = new RandomAccessFile(temp, "rw")
        f.setLength(1024 * 1024 * x)
        f.close()

        storage.store(temp)

        loader.exists(temp.getName) should be(true)

        storage.delete(temp.getName)

        loader.exists(temp.getName) should be(false)
      }
    }
  }
}

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("carrot.resource.store.bucket.result", "carrot.resource.loader.bucket.result"))
class BucketResultStorageTest extends WordSpec with Matchers {

  @Autowired
  val storage: ResourceStorage = null

  @Autowired
  val loader: ResourceLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "BucketResult access classes" should {
    "use result bucket" in {
      loader.asInstanceOf[BucketResultLoader].getBucketName should equal("wcmc-data-stasis-result-test")
      storage.asInstanceOf[BucketResultStorage].getBucketName should equal("wcmc-data-stasis-result-test")
    }

    "store" in {

      for (x <- 1 to 10 by 3) {
        val temp = File.createTempFile("temp", "x")
        temp.deleteOnExit()

        val f = new RandomAccessFile(temp, "rw")
        f.setLength(1024 * 1024 * x)
        f.close()

        storage.store(temp)

        loader.exists(temp.getName) should be(true)

        storage.delete(temp.getName)

        loader.exists(temp.getName) should be(false)
      }
    }
  }
}


@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array(
  "carrot.resource.store.bucket", "carrot.resource.loader.bucket",
  "carrot.resource.store.bucket.result", "carrot.resource.loader.bucket.result"
))
class MultipleBucketProfilesTest extends WordSpec with Matchers {

  @Autowired
  val storage: ResourceStorage = null

  @Autowired
  val loader: ResourceLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "BucketResult access classes" should {
    "use result bucket" in {
      loader.asInstanceOf[BucketResultLoader].getBucketName should equal("wcmc-data-stasis-result-test")
      storage.asInstanceOf[BucketResultStorage].getBucketName should equal("wcmc-data-stasis-result-test")
    }

    "store" in {

      for (x <- 1 to 10 by 3) {
        val temp = File.createTempFile("temp", "x")
        temp.deleteOnExit()

        val f = new RandomAccessFile(temp, "rw")
        f.setLength(1024 * 1024 * x)
        f.close()

        storage.store(temp)

        loader.exists(temp.getName) should be(true)

        storage.delete(temp.getName)

        loader.exists(temp.getName) should be(false)
      }
    }
  }
}
