package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.converter

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.QuantifiedSample

/**
  * converts a sample to a serializeable form
  */
trait SampleConverter[T, R] {

  /**
    * converts a sample to an different representation
    *
    * @param quantifiedSample
    * @return
    */
  def convert(quantifiedSample: QuantifiedSample[T]): R
}
