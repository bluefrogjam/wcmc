package edu.ucdavis.fiehnlab.cts3.server

import edu.ucdavis.fiehnlab.cts3.model.Hit
import org.springframework.web.bind.annotation.{PathVariable, RequestMapping, RequestParam, RestController}

@RestController(value = "/rest/convert")
class ConversionController {
  @RequestMapping(value = Array("/{from}/{to}/{keyword}"))
  def convert(@PathVariable from: String, @PathVariable to: String, @PathVariable keyword: String): Hit = {
    new Hit("", "", "", "", 0.0f)
  }
}
