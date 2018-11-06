package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.event

import java.util.Date

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

  /**
    * stage that generated the event
    */
  val stage: Option[String]
}

case class ProcessBeginEvent(sample: Sample, stage: Option[String] = None) extends WorkflowEvent

case class ProcessFinishedEvent(sample: Sample, stage: Option[String] = None) extends WorkflowEvent

case class PreProcessingBeginEvent(sample: Sample, stage: Option[String] = None) extends WorkflowEvent

case class PreProcessingFinishedEvent(sample: Sample, stage: Option[String] = None) extends WorkflowEvent

case class PostProcessingBeginEvent(sample: Sample, stage: Option[String] = None) extends WorkflowEvent

case class PostProcessingFinishedEvent(sample: Sample, stage: Option[String] = None) extends WorkflowEvent

case class CorrectionBeginEvent(sample: Sample, stage: Option[String] = None) extends WorkflowEvent

case class CorrectionFinishedEvent(sample: Sample, stage: Option[String] = None) extends WorkflowEvent

case class AnnotationBeginEvent(sample: Sample, stage: Option[String] = None) extends WorkflowEvent

case class AnnotationFinishedEvent(sample: Sample, stage: Option[String] = None) extends WorkflowEvent

case class QuantificationBeginEvent(sample: Sample, stage: Option[String] = None) extends WorkflowEvent

case class QuantificationFinishedEvent(sample: Sample, stage: Option[String] = None) extends WorkflowEvent

case class ExportBeginEvent(sample: Sample, stage: Option[String] = None) extends WorkflowEvent

case class ExportFinishedEvent(sample: Sample, stage: Option[String] = None) extends WorkflowEvent

case class MemoryMonitorEvent(sample: Sample, stage: Option[String] = None) extends WorkflowEvent
