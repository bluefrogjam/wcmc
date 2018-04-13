package edu.ucdavis.fiehnlab.cts3.api.conversion

import edu.ucdavis.fiehnlab.cts3.api.Converter
import edu.ucdavis.fiehnlab.cts3.model.Hit
import org.springframework.beans.factory.annotation.Autowired

/**
  * Created by diego on 1/12/2018
  **/
class SequentialConverter(@Autowired val converters: List[Converter]) extends Converter {
  logger.debug(s"Creating SequetialConverter with converters: ${converters.map(_.getClass.getSimpleName).mkString(";")}")

  override protected def doConvert(keyword: String, from: String, to: String): Seq[Hit] = {
    converters.map { cvt =>
      Hit("", "", "", "", 0.0f)
    }
  }

  override protected def supportsJSONLogging = false

  /**
    * which phase we require to log
    */
  override protected val phaseToLog = "none"

  override final def requires: Map[String, String] = {
    logger.debug(converters.sortBy(-_.priority).map(_.getClass.getSimpleName).mkString(" <- "))

    converters.sortBy(-_.priority).flatMap(_.requires.toSeq).toMap[String, String]
  }

  override final def provides: Map[String, String] = {
    converters.sortBy(-_.priority).flatMap(_.provides).toMap[String, String]
  }
}
