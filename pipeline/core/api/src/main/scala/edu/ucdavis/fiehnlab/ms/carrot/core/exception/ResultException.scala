package edu.ucdavis.fiehnlab.ms.carrot.core.exception

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample

/**
  * Created by wohlgemuth on 9/14/17.
  */
class ResultException(message: String,sample:Sample) extends Exception(message)
