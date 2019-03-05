package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.controller

import org.apache.logging.log4j.scala.Logging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.web.bind.annotation._
import org.springframework.web.client.RestOperations


/**
  * Created by matthewmueller on 5/11/17.
  */

@RestController
@RequestMapping(path = Array("/rest/similarity"))
class SimilarityController extends Logging {

  @Autowired
  val restOperations:RestOperations = null

  @ResponseBody
  @RequestMapping(path = Array("/search"), method = Array(RequestMethod.POST))
  @Async
  def similarity(@RequestBody request: SimilaritySearch): Array[SimilarityResponse] = {

    logger.info(s"searching for ${request.spectrum}")

    val result = restOperations.postForEntity("http://mona.fiehnlab.ucdavis.edu/rest/similarity/search", request, classOf[Array[SimilarityResponse]])

    logger.info(s"result: ${result}")

    result.getBody
  }

}

case class SimilaritySearch(spectrum: String)

case class SimilarityResponse(hit:SimilaritySearchHit,score:Double)
case class SimilaritySearchHit(spectrum:String,id:String,compound:Array[Compound],library:Library,metaData:Array[MetaData])
case class Compound(inchi:String,inchiKey:String,names:Array[Name])
case class Name(name:String)
case class MetaData(name:String,value:String)