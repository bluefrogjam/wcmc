package edu.ucdavis.fiehnlab.wcmc.api.rest.exposome4j

import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations

@Component
class ExposomeClient extends Logging {

  @Value("${wcmc.api.rest.exposome.host:exposome.fiehnlab.ucdavis.edu}")
  val host: String = ""

  @Value("${wcmc.api.rest.exposome.host:80}")
  val port: Int = 80

  @Autowired
  val restTemplate: RestOperations = null

  /** *
    * loads the exposme by name
    *
    * @param name
    */
  def loadByName(name: String) = {
    val data = Map("queryterm" -> name)

    val url = s"http://$host:$port/ocpu/library/bloodexposome3/R/searchCompoundName/json"
    logger.info(url)
    restTemplate.postForObject(url, data, classOf[Map[String, Any]])
  }


  /**
    * loads the exposome by inchi key
    *
    * @param inchiKey
    */
  def loadByInchiKey(inchiKey: String) = {
    val data = Map("ikterm" -> inchiKey)

    val url = s"http://$host:$port/ocpu/library/bloodexposome3/R/searchInchiKeys/json"
    logger.info(url)
    restTemplate.postForObject(url, data, classOf[Map[String, Any]])
  }

}
