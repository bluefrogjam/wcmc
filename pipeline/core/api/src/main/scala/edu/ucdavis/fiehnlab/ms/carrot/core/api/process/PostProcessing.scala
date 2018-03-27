package edu.ucdavis.fiehnlab.ms.carrot.core.api.process

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.QuantifiedSample

/**
  * Created by wohlg on 7/11/2016.
  */
abstract class PostProcessing[T] extends Process[QuantifiedSample[T], QuantifiedSample[T]] {

}
