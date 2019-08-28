package edu.ucdavis.fiehnlab.ms.carrot.core.api.process

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample

/**
  * Created by diego on 11/30/2016.
  */
abstract class PreProcessor extends Process[Sample, Sample] with Logging {

  /**
    * if implemented clears certain caches
    */
  def clearCache() = {}
}
