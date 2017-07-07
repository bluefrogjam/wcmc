package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.postprocessing

import edu.ucdavis.fiehnlab.ms.carrot.core.api.process.Process
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{QuantifiedSample, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.WorkflowProperties

/**
  * Created by wohlg on 7/11/2016.
  */
abstract class PostProcessing[T](val properties: WorkflowProperties) extends Process[QuantifiedSample[T], QuantifiedSample[T]] {

}
