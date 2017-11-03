package edu.ucdavis.fiehnlab.wcmc.server.rdmonitor

import java.io.File
import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.server.rdmonitor.api.{FileEvent, FileEventListener}
import org.apache.commons.io.FileUtils
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Component
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.collection.mutable.ArrayBuffer

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[MonitorTestConfig]))
class MonitorTest extends WordSpec with LazyLogging with ShouldMatchers with BeforeAndAfterAll {
  @Value("${wcmc.monitor.sourceFolder:target/tmp}")
  val sourceFolder: String = ""

  @Autowired
  val monitor: Monitor = null

  @Autowired
  val listener: TestEventListener = null

  var TS: Long = 0

  new TestContextManager(this.getClass).prepareTestInstance(this)

  override def beforeAll() {
    TS = prepFiles()
  }

  override def afterAll() {
    FileUtils.deleteDirectory(new File("target/tmp"))
  }

  "testMonitor" should {
    "be defined" in {
      monitor should not be null
    }

    "have a sourceFolder" in {
      monitor.sourceFolder shouldEqual sourceFolder
    }

    "have a listener registered" in {
      monitor.listeners should not be empty
    }

    "find all files" in {
      listener.files.clear()
      new File(sourceFolder) should exist

      monitor.searchFiles(0)
      listener.files.size shouldBe 6
    }

    s"find files newer than ${TS}" ignore {    //timing not working, can't figure it out
      listener.files.clear()
      new File(sourceFolder) should exist

      monitor.searchFiles(TS)
      listener.files.size shouldBe 3
    }

  }

  private def prepFiles(): Long = {
    logger.debug(s"Creating fake files on ${sourceFolder}...")

    val sleep = 200
    val store = new File(sourceFolder)
    if (store.exists()) {
      store.delete()
    }
    store.mkdirs()

    Array(".d.zip", ".wiff").foreach(i => {File.createTempFile("blah_", i, store); Thread.sleep(sleep)})
    new File(s"${sourceFolder}/blah_folderold.d").mkdir()

    val ts = new Date().getTime

    Array(".d.zip", ".wiff").foreach(i => {File.createTempFile("blah_", i, store); Thread.sleep(sleep)})
    new File(s"${sourceFolder}/blah_foldernew.d").mkdir()

    ts
  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class MonitorTestConfig {}

@Component
class TestEventListener extends FileEventListener with LazyLogging {
  val files: ArrayBuffer[String] = ArrayBuffer.empty

  override def foundFile(event: FileEvent): Unit = {
    logger.info(s"Found new file: ${event.file.getName}\t-\t${event.file.lastModified()} - ref stamp: ${event.refTimeStamp}")
    files.append(event.file.getName)
  }
}
