package edu.ucdavis.fiehnlab.cts3.server

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.cts3.api.conversion.CactusConverter
import edu.ucdavis.fiehnlab.cts3.model.Hit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation._

/**
  * Created by diego on 1/12/2018
  **/
@RestController
@RequestMapping(value = Array("/rest"))
class ConversionController extends LazyLogging {
  @Autowired
  val converter: CactusConverter = null

  @GetMapping(value = Array("/convert/{from}/{to}/{keyword}"))
  def convert(@PathVariable from: String, @PathVariable to: String, @PathVariable keyword: String): Seq[Hit] = {
    logger.info(s"Converting $from: $keyword to $to")
    converter.convert(keyword, from, to)
  }
}
