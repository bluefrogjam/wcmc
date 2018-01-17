package edu.ucdavis.fiehnlab.cts3.api

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.cts3.model.Hit
import org.springframework.cache.annotation.Cacheable

/**
  * Created by diego on 1/12/2018
  **/
trait Converter extends LazyLogging {
  @Cacheable
  final def convert(keyword: String, from: String, to: String): Seq[Hit] = {
    logger.debug(s"Called trait convert ($keyword, $from, $to)")
    if (canConvert(from, to)) {
      doConvert(keyword, from, to)
    } else {
      Seq.empty
    }
  }

  protected def priority = 0

  protected def doConvert(keyword: String, from: String, to: String): Seq[Hit]

  protected def canConvert(from: String, to:String): Boolean

  protected def supportsJSONLogging = false

  protected val phaseToLog = "none"

}
