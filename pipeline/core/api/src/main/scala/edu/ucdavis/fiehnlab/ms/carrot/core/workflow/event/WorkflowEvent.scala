package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.event

import java.util.Date

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment


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
  val experiment: Experiment
}

case class ProcessBeginEvent(experiment: Experiment) extends WorkflowEvent

case class ProcessFinishedEvent(experiment: Experiment) extends WorkflowEvent

case class PreProcessingBeginEvent(experiment: Experiment) extends WorkflowEvent

case class PreProcessingFinishedEvent(experiment: Experiment) extends WorkflowEvent

case class PostProcessingBeginEvent(experiment: Experiment) extends WorkflowEvent

case class PostProcessingFinishedEvent(experiment: Experiment) extends WorkflowEvent

case class CorrectionBeginEvent(experiment: Experiment) extends WorkflowEvent

case class CorrectionFinishedEvent(experiment: Experiment) extends WorkflowEvent

case class AnnotationBeginEvent(experiment: Experiment) extends WorkflowEvent

case class AnnotationFinishedEvent(experiment: Experiment) extends WorkflowEvent

case class QuantificationBeginEvent(experiment: Experiment) extends WorkflowEvent

case class QuantificationFinishedEvent(experiment: Experiment) extends WorkflowEvent

case class ExportBeginEvent(experiment: Experiment) extends WorkflowEvent

case class ExportFinishedEvent(experiment: Experiment) extends WorkflowEvent
