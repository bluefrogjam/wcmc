package edu.ucdavis.fiehnlab.ms.carrot.core.exception

import java.io.IOException

/**
  * thrown if we are trying to read a sample, we do not know yet how to parse
  */
class UnsupportedSampleException(message:String) extends IOException(message){

}
