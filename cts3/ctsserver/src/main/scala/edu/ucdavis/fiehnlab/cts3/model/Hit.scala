package edu.ucdavis.fiehnlab.cts3.model

import scala.beans.BeanProperty

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
case class Hit(
                  @BeanProperty keyword: String,
                  @BeanProperty from: String,
                  @BeanProperty to: String,
                  @BeanProperty result: String,
                  @BeanProperty score: Float
              )
