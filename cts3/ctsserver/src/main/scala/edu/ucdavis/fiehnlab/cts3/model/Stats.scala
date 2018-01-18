package edu.ucdavis.fiehnlab.cts3.model

import edu.ucdavis.fiehnlab.cts3.api.Converter

/**
  * Created by diego on 1/18/2018
  **/
trait Stats {
  val timing: Long
  val converter: Class[_]
}
