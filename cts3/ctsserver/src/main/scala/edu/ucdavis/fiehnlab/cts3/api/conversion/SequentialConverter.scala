package edu.ucdavis.fiehnlab.cts3.api.conversion

import edu.ucdavis.fiehnlab.cts3.api.Converter
import edu.ucdavis.fiehnlab.cts3.model.Hit

/**
  * Created by diego on 1/12/2018
  **/
class SequentialConverter(val converters: List[Converter]) extends Converter {
  override protected def doConvert(keyword: String, from: String, to: String): Seq[Hit] = {
    converters.map { cvt =>
        Hit("","","","",0.0f)
    }
  }

  override protected def supportsJSONLogging = false

  /**
    * which phase we require to log
    */
  override protected val phaseToLog = "none"

  override protected def requires: Map[String, String] = Map.empty

  override protected def provides: Map[String, String] = Map.empty
}
