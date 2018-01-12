package edu.ucdavis.fiehnlab.cts3.server

import edu.ucdavis.fiehnlab.cts3.model.Hit
import org.springframework.web.bind.annotation.{RequestMapping, RequestParam, RestController}

@RestController(value = "/rest/convert")
class ConversionController {
  @RequestMapping("/{from}/{to}/{keyword}")
  def convert(from: String, to: String, keyword: String): Hit = {
    new Hit("", "","","",0.0)
  }
}
