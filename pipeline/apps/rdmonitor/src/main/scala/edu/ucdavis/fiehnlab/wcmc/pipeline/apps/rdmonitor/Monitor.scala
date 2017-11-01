package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.rdmonitor

import java.io.File
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}
import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.scheduling.annotation.{EnableScheduling, Scheduled}

import scala.collection.mutable.ArrayBuffer

@SpringBootApplication
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
@EnableScheduling
class Monitor extends CommandLineRunner with LazyLogging {
  @Value("${wcmc.monitor.sourceFolder:h:\\p20repo}")
  val sourceFolder: String = null

  @Value("${wcmc.monitor.timestamp:0}")
  val timestamp: Long = 0

  val rawDataFileExtensions: Seq[String] = Seq("d.zip", "raw", "wiff")
  private var rawFiles: ArrayBuffer[File] = ArrayBuffer.empty[File]

  override def run(args: String*): Unit = {

  }

  @Scheduled(fixedRate = 60 * 1000)
  def monitor(): Unit = {
    logger.info("The time is now {}", new Date())

    //getFiles()

    //send to conversion (up to 4 parallel)

    //update timestamp (saving to config)
  }

  def getFiles(timestamp: Long = timestamp): Seq[File] = {
    logger.debug(s"Start at timestamp: ${timestamp}")

    rawFiles.clear()

    val d = new File(sourceFolder)
    if (d.exists && d.isDirectory) {

      Files.walkFileTree(d.toPath, new SimpleFileVisitor[Path] {
        override def preVisitDirectory(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
          if (attrs.isDirectory && (attrs.lastModifiedTime.toMillis > timestamp) && file.toString.toLowerCase().endsWith(".d")) {
            rawFiles.append(file.toFile)
            FileVisitResult.SKIP_SUBTREE
          } else if (attrs.isDirectory && file.getFileName.toString.toLowerCase().contains("acqdata")) {
            FileVisitResult.SKIP_SIBLINGS
          } else {
            FileVisitResult.CONTINUE
          }
        }

        override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
          if (attrs.isRegularFile && attrs.lastModifiedTime().toMillis > timestamp && rawDataFileExtensions.exists(ext => file.getFileName.toString.toLowerCase().endsWith(ext))) {
            rawFiles.append(file.toFile)
          }
          FileVisitResult.CONTINUE
        }
      })

      rawFiles.sortBy(_.lastModified())
    }

    else {
      logger.warn(s"Invalid folder ${d.getName}")
      Seq[File]()
    }
  }

}
