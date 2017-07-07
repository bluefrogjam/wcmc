package edu.ucdavis.fiehnlab.wcms.api.rest.ossa4j

import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations

/**
  * Created by wohlgemuth on 6/29/17.
  */
@Component
class Ossa4JClient extends LazyLogging{

  @Value("${wcms.api.rest.ossa4j.host:127.0.0.1}")
  val host: String = ""

  @Value("${wcms.api.rest.ossa4j.port:8080}")
  val port: Int = 80

  @Autowired
  val restTemplate: RestOperations = null

  /**
    * adds a new library spectra to the internally used library
    * @param id
    * @param spectrum
    * @return
    */
  def addLibrarySpectrum(id:String, spectrum:String) = {

    val data = Map("upload" -> Map("spectrum" -> spectrum, "id" -> id))

    val result = restTemplate.postForObject(s"http://${host}:${port}/similarity/add",data,classOf[Map[String,Float]])
    logger.trace(s"result: ${result}")
  }

  /**
    * clears all the spectra from the library
    * @return
    */
  def clear = restTemplate.postForLocation(s"http://${host}:${port}/similarity/clear",null)

  /**
    * commits all the recently added library spectra to
    * the GPU method
    *
    * @return
    */
  def commit = restTemplate.put(s"http://${host}:${port}/similarity/commit",null)

  /**
    * returns the size of the libary used for calculations
    * @return
    */
  def librarySize:Int = {
    val result = restTemplate.getForObject(s"http://${host}:${port}/similarity/librarySize",classOf[Map[String,Int]],None)

    logger.trace(s"result: ${result}")
    result("result")
  }

  /**
    * searches against the libray and returns all spectra with a score larger than
    * minimumScore
    *
    * @param spectrum
    * @param minimumScore
    * @return
    */
  def search(spectrum:String, minimumScore:Double):List[Result] = {
    val data = Map("searchRequest" -> Map("spectrum" -> spectrum, "minimumScore" -> minimumScore))
    val result = restTemplate.postForObject(s"http://${host}:${port}/similarity/search",data,classOf[Map[String,List[Map[String,Any]]]])
    logger.info(s"result: ${result}")

    result("result").map{ x =>
      Result(x("splash").asInstanceOf[String],x("score").asInstanceOf[Double])
    }
  }

}

case class Result(splashKey:String, score:Double)