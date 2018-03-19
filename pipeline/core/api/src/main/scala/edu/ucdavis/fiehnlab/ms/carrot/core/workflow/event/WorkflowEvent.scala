package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.event

import java.util.Date

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample


/**
  * utilized to inform other parts of the processing regarding the state of calculation
  */
sealed trait WorkflowEvent {

  /**
    * when did this even occur
    */
  final val time:Date = new Date()

  /**
    * associated experiment, for this event
    */
  val sample: Sample
}

case class ProcessBeginEvent(sample: Sample) extends WorkflowEvent

case class ProcessFinishedEvent(sample: Sample) extends WorkflowEvent

case class PreProcessingBeginEvent(sample: Sample) extends WorkflowEvent

case class PreProcessingFinishedEvent(sample: Sample) extends WorkflowEvent

case class PostProcessingBeginEvent(sample: Sample) extends WorkflowEvent

case class PostProcessingFinishedEvent(sample: Sample) extends WorkflowEvent

case class CorrectionBeginEvent(sample: Sample) extends WorkflowEvent

case class CorrectionFinishedEvent(sample: Sample) extends WorkflowEvent

case class AnnotationBeginEvent(sample: Sample) extends WorkflowEvent

case class AnnotationFinishedEvent(sample: Sample) extends WorkflowEvent

case class QuantificationBeginEvent(sample: Sample) extends WorkflowEvent

case class QuantificationFinishedEvent(sample: Sample) extends WorkflowEvent

case class ExportBeginEvent(sample: Sample) extends WorkflowEvent

case class ExportFinishedEvent(sample: Sample) extends WorkflowEvent
