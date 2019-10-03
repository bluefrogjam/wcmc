package edu.ucdavis.fiehnlab.loader.storage

import java.io.{File, FileWriter}

import edu.ucdavis.fiehnlab.loader.TestConfiguration
import org.apache.logging.log4j.scala.Logging
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TestConfiguration]))
@ActiveProfiles(Array("carrot.resource.store.local"))
class FileStorageTest extends WordSpec with Matchers with Logging {

  @Autowired
  val storage: FileStorage = null

  @Autowired
  val properties: FileStorageProperties = null

  new TestContextManager(this.getClass).prepareTestInstance(this)
  "FileStorageTest" should {

    "directory" in {
      properties.directory shouldBe "local_storage"
    }
    "create a temp file" should {
      val file = File.createTempFile("dadssa", "test")
      val writer = new FileWriter(file)
      for (i <- 0.to(1024)) {
        for (y <- 0.to(10)) {
          writer.append("a")
        }
      }
      writer.close()

      for (x <- 1 to 5) {
        s"store it several time ${x}" in {


          storage.store(file)

          val storedFile = new File(properties.directory, file.getName)

          storedFile.exists() shouldBe true
        }
      }

      "delete it" in {
        val storedFile = new File(properties.directory, file.getName)

        storedFile.exists() shouldBe true

        storage.delete(file.getName)

        storedFile.exists() shouldBe false

      }
    }


  }
}
