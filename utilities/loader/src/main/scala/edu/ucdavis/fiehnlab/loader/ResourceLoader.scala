package edu.ucdavis.fiehnlab.loader

import java.io._
import java.nio.file._

import javax.annotation.PostConstruct
import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

/**
  * Interface for loading resources dynamically independent of actual representation
  */
trait ResourceLoader extends Logging {

  /**
    * returns the related resource or none
    *
    * @param name
    * @return
    */
  def load(name: String): Option[InputStream]

  /**
    * will load the resource as file, by utilizing a TEMP directory
    * should be avoided due to uneccesaery performance overhead, but some tools
    * sadly require files and can't handle streams
    *
    * @param name
    * @return
    */
  def loadAsFile(name: String): Option[File] = {

    val loaded = try {
      load(name)
    } catch {
      case error: Exception => {
        logger.debug(s"Error in ${this.getClass.getSimpleName} = ${error.getMessage}")
      }
        None
    }

    if (loaded.isDefined) {
      val fName = if (name.startsWith("/")) {
        name.substring(1)
      } else {
        name
      }

      val tempFile = new File(Files.createTempDirectory("fserv").toFile, fName)
      logger.debug(s"storing ${fName} at: ${tempFile.getAbsolutePath}")
      tempFile.deleteOnExit()

      try {
        Files.copy(loaded.get, Paths.get(tempFile.toURI))
      } catch {
        case x: FileAlreadyExistsException =>
          logger.warn(s"reusing existing file: ${x.getMessage}")
      }

      Option(tempFile)
    } else {
      logger.debug(s"File ${name} not found")
      None
    }

  }

  /**
   * internal storage source path
   */
  def getSource: String

  /**
    * priority of the loader
    *
    * @return
    */
  def priority: Int = 0

  /**
    * does the given resource exists
    *
    * @param name
    * @return
    */
  def exists(name: String): Boolean

  /**
    * check if the requested resource is a directory
    *
    * @param name
    * @return
    */
  def isDirectory(name: String): Boolean = ???

  /**
    * checks if the requested resource is a file
    *
    * @param name
    * @return
    */
  def isFile(name: String): Boolean = ???

  override def toString: String = {
    s"${getClass.getSimpleName} (priority: $priority)"
  }
}

/**
  * attempets to load resources from different defined loaders
  */
@Component
@Primary
class DelegatingResourceLoader extends ResourceLoader {

  /**
    * all registered loaders, based on the spring enviornment
    */
  @Autowired
  val loaders: java.util.List[ResourceLoader] = null

  /**
    * sorted by priority
    */
  lazy val sortedLoaders: Seq[ResourceLoader] = loaders.asScala.sortBy(_.priority).reverse

  /**
    * tries to find the resource in any of the defined loaders or null
    *
    * @param name
    * @return
    */

  // This is ugly as hell but the previous implementation failed to load a sample from remote loader
  override def load(name: String): Option[InputStream] = sortedLoaders.collect {
    case loader =>
      loader.load(name)
  }.filter(_.isDefined) match {
    case empty if empty.size < 1 => None
    case full => full.head
  }

  override def toString: String = super.toString.concat(s"[${sortedLoaders.map(_.toString())}]")

  override def exists(name: String): Boolean = sortedLoaders.exists { loader =>
    val result = loader.exists(name)
    logger.debug(s"evaluation of ${loader} for ${name} is ${result}")
    result
  }

  @PostConstruct
  def init(): Unit = {
    logger.info(s"configured with the following (${sortedLoaders.size}) resource loaders: ${sortedLoaders}")
  }

  /**
   * internal storage source path
   */
  override def getSource: String = sortedLoaders.map { loader =>
    loader.getSource
  }.mkString(" | ")
}

/**
  * accesses local resources on the filesystem
  */
trait LocalLoader extends ResourceLoader

/**
  * access remote resources
  */
trait RemoteLoader extends ResourceLoader {

  /**
    * is a server allowed to use this one for lookup
    * functionality
    */
  def isLookupEnabled: Boolean
}
