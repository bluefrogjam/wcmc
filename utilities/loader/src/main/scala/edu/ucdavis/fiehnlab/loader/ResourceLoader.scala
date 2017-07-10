package edu.ucdavis.fiehnlab.loader

import java.io.InputStream

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
    * priority of the loader
    *
    * @return
    */
  def priority: Int = 0

  /**
    * does the given resource exists
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
}

trait LocalLoader extends ResourceLoader

trait RemoteLoader extends ResourceLoader