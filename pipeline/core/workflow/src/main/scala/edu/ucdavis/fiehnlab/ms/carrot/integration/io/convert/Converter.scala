package edu.ucdavis.fiehnlab.ms.carrot.integration.io.convert

import org.springframework.messaging.Message

/**
  * Created by wohlgemuth on 3/13/17.
  */
trait Converter[IN,OUT] {

  /**
    * converts something from an input to an output
    * @param input
    * @return
    */
  def convert(input:Message[IN]) : Message[OUT]
}
