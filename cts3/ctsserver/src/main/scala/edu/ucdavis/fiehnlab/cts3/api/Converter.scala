package edu.ucdavis.fiehnlab.cts3.api

import edu.ucdavis.fiehnlab.cts3.model.Hit
import org.springframework.cache.annotation.Cacheable

trait Converter {
  @Cacheable
  final def convert(keyword: String, from: String, to: String): Seq[Hit] = {
    doConvert(keyword, from, to)
  }

  protected def doConvert(keyword: String, from: String, to: String): Seq[Hit]

  protected def canConvert(from: String, to:String): Boolean

  protected def supportsJSONLogging = false

  protected val phaseToLog = "none"

}
