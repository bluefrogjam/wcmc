package edu.ucdavis.fiehnlab.wcmc.server.rdmonitor

import java.io._
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.server.rdmonitor.api.{FileEventListener, NewFileEvent}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.{EnableScheduling, Scheduled}
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

@Component
class Monitor extends CommandLineRunner with LazyLogging {
  @Value("${wcmc.monitor.sourceFolder:/storage}")
  val sourceFolder: String = null

  @Value("${wcmc.monitor.timestamp:0}")
  var timestamp: Long = 0

  @Autowired(required = false)
  val listeners: java.util.List[FileEventListener] = List[FileEventListener]().asJava

  val rawDataFileExtensions: Seq[String] = Seq("d.zip", "raw", "wiff")

  override def run(args: String*): Unit = {}

  @Scheduled(fixedRate = 60*60*24 * 1000) // run daily
  def monitor(): Unit = {
    searchFiles(timestamp)
  }

  def searchFiles(timestamp: Long = timestamp): Unit = {
    val d = new File(sourceFolder)
    if (d.exists && d.isDirectory) {

      Files.walkFileTree(d.toPath, new SimpleFileVisitor[Path] {
        override def preVisitDirectory(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
          if (attrs.isDirectory && attrs.lastModifiedTime.toMillis > timestamp && file.toString.toLowerCase().endsWith(".d")) {
            newFileFound(file.toFile)

            FileVisitResult.SKIP_SUBTREE
          } else if (attrs.isDirectory && file.getFileName.toString.toLowerCase().contains("acqdata")) {
            FileVisitResult.SKIP_SIBLINGS
          } else {
            FileVisitResult.CONTINUE
          }
        }

        override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
          if (attrs.isRegularFile && attrs.lastModifiedTime().toMillis > timestamp &&
              rawDataFileExtensions.exists(ext => file.getFileName.toString.toLowerCase().endsWith(ext))) {

            newFileFound(file.toFile)
          }
          FileVisitResult.CONTINUE
        }
      })
    } else {
      logger.warn(s"Invalid folder ${d.getName}")
    }
  }

  private def newFileFound(file: File): Unit = {
    if(listeners.asScala.nonEmpty) {
      listeners.asScala.foreach(_.foundFile(NewFileEvent(file, timestamp)))
    }
  }
}

@Configuration
@ConditionalOnProperty(value = Array("scheduling.enabled"), havingValue = "true", matchIfMissing = true)
@EnableScheduling
class SchedulingConfiguration{}
