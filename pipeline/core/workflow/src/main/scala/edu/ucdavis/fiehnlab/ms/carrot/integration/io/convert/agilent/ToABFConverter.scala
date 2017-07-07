package edu.ucdavis.fiehnlab.ms.carrot.integration.io.convert.agilent

import java.io.File

import edu.ucdavis.fiehnlab.ms.carrot.integration.io.convert.Converter
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.messaging.Message
import org.springframework.stereotype.Component

/**
  * Created by wohlgemuth on 3/13/17.
  */
@Component
class ToABFConverter extends Converter[File,File]{

  @ServiceActivator
  def convert(message:Message[File]) : Message[File] = null
}
