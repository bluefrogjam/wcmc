package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.scanner.listeners

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample

/**
  * this listener should be fired, every time we detected a sample
  */
trait SampleScannerListener {
  /**
    * a valid sample, which can be processed by the system
    *
    * @param sample
    */
  def found(sample: Sample): Unit

}
