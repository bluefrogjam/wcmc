package edu.ucdavis.fiehnlab.cts3.api

import edu.ucdavis.fiehnlab.cts3.model.Hit

class SequentialConverter(val converters: List[Converter]) extends Converter {
  override protected def doConvert(keyword: String, from: String, to: String): Seq[Hit] = {
      converters.foreach { converter =>
      }
  }

  override protected def supportsJSONLogging = false

  /**
    * which phase we require to log
    */
  override protected val phaseToLog = "none"
}
