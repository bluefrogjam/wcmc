package edu.ucdavis.fiehnlab.ms.carrot.core.api.process

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample

/**
  * Created by diego on 11/30/2016.
  */
abstract class PreProcessor extends Process[Sample, Sample] with LazyLogging {}
