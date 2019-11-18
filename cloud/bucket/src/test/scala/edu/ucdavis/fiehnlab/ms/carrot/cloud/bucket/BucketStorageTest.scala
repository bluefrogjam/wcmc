package edu.ucdavis.fiehnlab.ms.carrot.cloud.bucket

import java.io.{File, RandomAccessFile}

import edu.ucdavis.fiehnlab.loader.impl.DirectoryResourceLoader
import edu.ucdavis.fiehnlab.loader.storage.{FileStorage, FileStorageProperties}
import edu.ucdavis.fiehnlab.loader.{ResourceLoader, ResourceStorage}
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("carrot.resource.store.bucket.data", "carrot.resource.loader.bucket.data"))
class BucketStorageTest extends WordSpec with Matchers {

  @Autowired
  @Qualifier("dataStorage")
  val storage: ResourceStorage = null

  @Autowired
  @Qualifier("dataLoader")
  val loader: ResourceLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Bucket data access classes" should {
    "use data bucket" in {
      loader.getSource should equal("datatest-carrot")
      storage.getDestination should equal("datatest-carrot")
    }

    "store" in {

      for (x <- 1 to 10 by 3) {
        val temp = File.createTempFile("temp", "x")
        temp.deleteOnExit()

        val f = new RandomAccessFile(temp, "rw")
        f.setLength(1024 * 1024 * x)
        f.close()

        storage.store(temp)

        storage.exists(temp.getName) should be(true)

        storage.delete(temp.getName)

        storage.exists(temp.getName) should be(false)
      }
    }
  }
}

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("carrot.resource.store.bucket.result", "carrot.resource.loader.bucket.result"))
class BucketResultStorageTest extends WordSpec with Matchers {

  @Autowired
  @Qualifier("resultStorage")
  val storage: ResourceStorage = null

  @Autowired
  @Qualifier("resultLoader")
  val loader: ResourceLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "BucketResult access classes" should {
    "use result bucket" in {
      loader.getSource should equal("wcmc-data-stasis-result-test")
      storage.getDestination should equal("wcmc-data-stasis-result-test")
    }

    "store" in {

      for (x <- 1 to 10 by 3) {
        val temp = File.createTempFile("temp", "x")
        temp.deleteOnExit()

        val f = new RandomAccessFile(temp, "rw")
        f.setLength(1024 * 1024 * x)
        f.close()

        storage.store(temp)

        storage.exists(temp.getName) should be(true)

        storage.delete(temp.getName)

        storage.exists(temp.getName) should be(false)
      }
    }
  }
}


@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array(
  "carrot.resource.store.bucket.data", "carrot.resource.loader.bucket.data",
  "carrot.resource.store.bucket.result", "carrot.resource.loader.bucket.result",
  "carrot.resource.store.local", "carrot.resouce.loader.local"
))
class MultipleBucketProfilesTest extends WordSpec with Matchers with Logging {

  @Autowired
  @Qualifier("dataStorage")
  val dataStorage: ResourceStorage = null

  @Autowired
  @Qualifier("resultLoader")
  val resultLoader: ResourceLoader = null

  @Autowired
  @Qualifier("localStorage")
  val localStorage: ResourceStorage = null

  @Autowired
  @Qualifier("localLoader")
  val localLoader: ResourceLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "BucketResult access classes" should {
    "have correct destinations" in {
      resultLoader.getSource should equal("wcmc-data-stasis-result-test")
      dataStorage.getDestination should equal("datatest-carrot")
      localStorage.getDestination should equal("local_storage")
      localLoader.getSource should equal("local_storage")
    }

    "load locally, store remotely" in {
      new File("local_storage").mkdirs()
      val tmpfile: File = new File("local_storage/test.txt")
      tmpfile.deleteOnExit()
      logger.info(s"creating tmp file: $tmpfile")

      val f = new RandomAccessFile(tmpfile, "rw")
      f.setLength(1024 * 1024 * 5)
      f.close()

      dataStorage.store(localLoader.loadAsFile(tmpfile.getName).get)

      dataStorage.exists(tmpfile.getName) should be(true)

      dataStorage.delete(tmpfile.getName)

      dataStorage.exists(tmpfile.getName) should be(false)
    }
  }
}


@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class BucketTestConfiguration() {
  @Autowired
  val fileStorageProperties: FileStorageProperties = null

  @Bean(name = Array("localStorage"))
  def storage: ResourceStorage = new FileStorage(fileStorageProperties)

  @Bean(name = Array("localLoader"))
  def loader: ResourceLoader = new DirectoryResourceLoader(new File(fileStorageProperties.directory))
}
