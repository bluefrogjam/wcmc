package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.rdmonitor

import java.io.File
import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[MonitorTestConfig]))
class MonitorTest extends WordSpec with LazyLogging with ShouldMatchers {
  @Autowired
  val monitor: Monitor = null

  // right after Wed Aug 16 08:43:57 PDT 2017 but before Wed Aug 17 09:41:00 PDT 2017
  val TS: Long = 1502898800000L

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "testMonitor" should {
    "be defined" in {
      monitor should not be null
    }

    "have a sourceFolder" in {
      monitor.sourceFolder shouldEqual "h:\\p20repo"
    }

    "list all raw files in 'sourceFolder' from timestamp 0" in {
      val files = monitor.getFiles()
      val strFiles = files.map {
        _.getName
      }

      files should have size 8
      strFiles should contain("B5_SA0259_P20Lipids_Pos_1FV_2392_MSMS.d")
      strFiles should contain("testA.d.zip")

      logger.debug(files.map { f => s"${new Date(f.lastModified())} - (${f.lastModified()}) - ${f.getName}" }.mkString("\n"))
    }

    "list raw files in 'sourceFolder' older than timestamp" in {
      val files: Seq[File] = monitor.getFiles(TS)
      val strFiles: Seq[String] = files.map {
        _.getName
      }

      files.minBy(_.lastModified()).lastModified() > TS
      files.filter(_.lastModified() > TS) should have size 3
      strFiles should contain("testA.d.zip")
      strFiles should not contain ("B5_SA0259_P20Lipids_Pos_1FV_2392_MSMS.d")
    }
  }
}

@SpringBootApplication(exclude = Array(classOf[MonitorAutoConfiguration]))
class MonitorTestConfig {
}
