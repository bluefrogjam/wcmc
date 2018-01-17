package edu.ucdavis.fiehnlab.cts3.model

/**
  * Actual result for a unique combination of conversion parameters
  *
  * @param keyword dirty string to convert
  * @param from    type of id to convert from
  * @param to      type of id to convert to
  * @param result  conversion result
  * @param score   conversion score
  *
  * Created by diego on 01/12/2018
  */
class Hit(val keyword: String, val from: String, val to: String, val result: String, val score: Float) {
  override def toString: String = {
    s"Keyword: $keyword, From: $from, To: $to, Result: $result, Score: $score"
  }
}
