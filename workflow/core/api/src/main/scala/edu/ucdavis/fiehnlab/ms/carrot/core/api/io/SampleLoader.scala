package edu.ucdavis.fiehnlab.ms.carrot.core.api.io

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample

/**
  * used to simply load samples from a local or remote storage depending on
  * implementation
  */
trait SampleLoader {

  /**
    * loads a sample
    *
    * @param name
    * @return
    */
  def loadSample(name: String): Option[Sample]

  /**
    * checks if the sample exist
    *
    * @param name
    * @return
    */
  def sampleExists(name: String): Boolean
}
