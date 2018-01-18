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
      if(from.eq(to)) {
        Seq(Hit(keyword, from, to, keyword, 1.0f))
      } else {
        doConvert(keyword, from, to)
      }
    } else {
      Seq.empty
    }
  }

  protected def requires: Map[String, String]

  protected def provides: Map[String, String]

  protected def priority = 0

  protected def doConvert(keyword: String, from: String, to: String): Seq[Hit]

  final def canConvert(from: String, to:String): Boolean = {
    requires.keySet.contains(from) && provides.keySet.contains(to)
  }

  protected def supportsJSONLogging = false

  protected val phaseToLog = "none"

}
