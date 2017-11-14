package edu.ucdavis.fiehnlab.wcmc.server.rdmonitor

import java.io.File
import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.server.rdmonitor.api.FileEventListener
import org.apache.commons.io.FileUtils
import org.junit.runner.RunWith
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, ShouldMatchers, WordSpec}
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration, Import, Profile}
import org.springframework.stereotype.Component
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.collection.mutable.ArrayBuffer

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[MonitorTestConfig]))
@ActiveProfiles(Array("test"))
class MonitorTest extends WordSpec with LazyLogging with ShouldMatchers with BeforeAndAfterAll with Eventually with IntegrationPatience {
  @Value("${wcmc.monitor.sourceFolder:target/tmp}")
  val sourceFolder: Array[String] = null

  @Autowired
  val monitor: Monitor = null

  @Autowired
  val listener: TestEventListener = null

  @Autowired
  val rabbitTemplate: RabbitTemplate = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  var TS: Long = 0

  override def beforeAll() {
    FileUtils.deleteDirectory(new File("target/tmp"))

    TS = prepFiles()
  }

  override def afterAll() {
    FileUtils.deleteDirectory(new File("target/tmp"))
  }

  "testMonitor" should {
    "be defined" ignore {
      monitor should not be null
    }

    "have a sourceFolder" ignore {
      monitor.sourceFolder shouldEqual sourceFolder
    }

    "have a listener registered" ignore {
      monitor.listeners should not be empty
    }

    "find all files" ignore {
      listener.files.clear()
      sourceFolder.foreach { f =>
        new File(f) should exist
      }

      monitor.searchFiles(0, sourceFolder)
      eventually(timeout(Span(5, Seconds))) {
        listener.files.size shouldBe 6 * sourceFolder.length
      }
    }

    s"find files newer than ${TS}" ignore { //timing not working, can't figure it out
      listener.files.clear()
      sourceFolder.foreach { f =>
        new File(f) should exist
      }

      monitor.searchFiles(TS, sourceFolder)
      eventually(timeout(Span(5, Seconds))) {
        listener.files.size shouldBe 3
      }
    }

  }

  private def prepFiles(): Long = {
    var ts: Long = 0

    val sleep = 200
    sourceFolder.zipWithIndex.foreach { case (f, ctr) =>
      val store = new File(f)
      if (store.exists()) {
        store.delete()
      }
      store.mkdirs()

      Array(".d.zip", ".wiff").foreach(i => {
        File.createTempFile("blah_", i, store)
        Thread.sleep(sleep)
      })
      new File(s"${f}/blah_folderold.d").mkdir()

      ts = new Date().getTime

      Array(".d.zip", ".wiff").foreach(i => {
        File.createTempFile("blah_", i, store)
        Thread.sleep(sleep)
      })
      new File(s"${f}/blah_foldernew.d").mkdir()
    }

    ts
  }
}

@Configuration
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[MonitorAutoConfiguration]))
class MonitorTestConfig {
  @Bean
  def listenerAdapter(receiver: FileEventListener) = new MessageListenerAdapter(receiver, "recieveMessage")
}

@Component
@Profile(Array("test"))
class TestEventListener extends FileEventListener with LazyLogging {
  val files: ArrayBuffer[String] = ArrayBuffer.empty

  def recieveMessage(message: FileMessage): Unit = {
    logger.info(s"Found new file: ${message.name}\t-\t${message.timestamp}")
    files.append(message.name)
  }
}
