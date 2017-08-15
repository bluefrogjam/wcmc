package edu.ucdavis.fiehnlab.ms.carrot.core.api.exception

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target

/**
  * Created by wohlgemuth on 8/15/17.
  */
class TargetGenerationNotSupportedException(s: String,val target:Target) extends Exception(s)
