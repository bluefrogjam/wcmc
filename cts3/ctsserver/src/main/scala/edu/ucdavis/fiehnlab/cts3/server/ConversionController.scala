package edu.ucdavis.fiehnlab.cts3.server

import com.google.gson.GsonBuilder
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.cts3.api.conversion.CactusConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation._

/**
  * Created by diego on 1/12/2018
  **/
@RestController
@RequestMapping(value = Array("/rest"))
class ConversionController extends LazyLogging {
  @Autowired
  val converter: CactusConverter = null

  @GetMapping(value = Array("/convert/{from}/{to}/{keyword}"), produces = Array(MediaType.APPLICATION_JSON_VALUE))
  def convert(@PathVariable from: String, @PathVariable to: String, @PathVariable keyword: String): String = {
    logger.info(s"Converting $from: $keyword to $to")
    val response = converter.convert(keyword, from, to)
    logger.info(s"Response in controller: $response")
    val gson = new GsonBuilder().disableHtmlEscaping().create()
    gson.toJson(response.toArray)
  }
}
