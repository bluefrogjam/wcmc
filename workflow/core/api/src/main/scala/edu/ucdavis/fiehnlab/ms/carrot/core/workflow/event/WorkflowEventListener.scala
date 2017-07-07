package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.event

/**
  * main event listener
  */
trait WorkflowEventListener {

  /**
    * informs the listener about a workflow event
    * @param workflowEvent
    */
  def handle(workflowEvent: WorkflowEvent)
}
