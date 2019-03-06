package edu.ucdavis.fiehnlab.wcmc.server.rdmonitor

import java.io._
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.wcmc.server.rdmonitor.api.FileEventListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

@Component
class Monitor extends CommandLineRunner with Logging {
  @Value("${wcmc.monitor.rabbit.queue:monitor-newFile-queue}")
  val queueName: String = ""

  @Value("${wcmc.monitor.rabbit.host:localhost}")
  val rabbitHost: String = ""

  @Value("${wcmc.monitor.sourceFolder:/storage}")
  val sourceFolder: Array[String] = null

  @Value("${wcmc.monitor.timestamp:0}")
  var timestamp: Long = 0

  @Autowired
  val rabbitTemplate: RabbitTemplate = null

  @Autowired
  val receiver: FileEventListener = null

  @Autowired
  val listeners: java.util.List[FileEventListener] = List[FileEventListener]().asJava

  val rawDataFileExtensions: Seq[String] = Seq("d.zip", "raw", "wiff")

  override def run(args: String*): Unit = {}

  def searchFiles(timestamp: Long = timestamp, srcfolder: Array[String]): Unit = {
    srcfolder.foreach { folder =>
      val d = new File(folder)
      if (d.exists && d.isDirectory) {

        Files.walkFileTree(d.toPath, new SimpleFileVisitor[Path] {
          override def preVisitDirectory(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
            if (attrs.isDirectory && attrs.lastModifiedTime.toMillis > timestamp && file.toString.toLowerCase().endsWith(".d")) {

              rabbitTemplate.convertAndSend(queueName, file.toAbsolutePath)

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

              rabbitTemplate.convertAndSend(queueName, file.toAbsolutePath)
            }
            FileVisitResult.CONTINUE
          }
        })
      } else {
        logger.warn(s"Invalid folder ${d.getName}")
      }
    }
  }
}

@Configuration
@ConditionalOnProperty(value = Array("wcmc.monitor.scheduling.enabled"), havingValue = "true", matchIfMissing = true)
@EnableScheduling
class SchedulingConfiguration {}


case class FileMessage(name:String, timestamp: Long)
