package edu.ucdavis.fiehnlab.loader

import java.io._
import javax.annotation.PostConstruct

import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._

/**
  * Interface for loading resources dynamically independent of actual representation
  */
trait ResourceLoader extends LazyLogging {

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
    val loaded = load(name)

    if (loaded.isDefined) {
      val fName = if(name.startsWith("/")){
        name.substring(1)
      }
      else{
        name
      }

      val tempFile = File.createTempFile("loader-temp", s"$fName")
      tempFile.deleteOnExit()

      val outStream = new FileWriter(tempFile)
      val stream = new InputStreamReader(loaded.get)
      Iterator
        .continually(stream.read)
        .takeWhile(-1 !=)
        .foreach { x =>
          outStream.write(x)
        }

      outStream.flush()
      outStream.close()
      stream.close()
      Option(tempFile)
    }
    else {
      None
    }
  }

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
}

/**
  * attempets to load resources from different defined loaders
  */
@Component
class DelegatingResourceLoader extends ResourceLoader {

  /**
    * all registered loaders, based on the spring enviornment
    */
  @Autowired
  val loaders: java.util.List[ResourceLoader] = null

  /**
    * sorted by priority
    */
  lazy val sortedLoader: List[ResourceLoader] = loaders.asScala.sortBy(_.priority).toList

  /**
    * trys to find the resource in any of the defined loaders or null
    *
    * @param name
    * @return
    */
  override def load(name: String): Option[InputStream] = sortedLoader.collectFirst { case loader if loader.load(name).isDefined => loader.load(name) }.getOrElse(None)

  override def toString = s"DelegatingResourceLoader($sortedLoader)"

  override def exists(name: String): Boolean = sortedLoader.collectFirst { case loader if loader.exists(name) => true }.getOrElse(false)

  @PostConstruct
  def init(): Unit ={
    logger.info(s"configured with the following resource loaders: ${loaders}")
  }
}

trait LocalLoader extends ResourceLoader

trait RemoteLoader extends ResourceLoader