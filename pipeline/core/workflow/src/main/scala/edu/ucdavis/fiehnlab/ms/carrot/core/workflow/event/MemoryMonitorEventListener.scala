package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.event

import com.typesafe.scalalogging.LazyLogging
import javax.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
case class MemoryMonitorEventListener() extends WorkflowEventListener with LazyLogging {

  @PostConstruct
  def printMem(): Unit = {
    logger.info(s"JVM_MAX_MEM=${Runtime.getRuntime.maxMemory() / 1024 / 1024}Mb")
  }

  /**
    * informs the listener about a workflow event
    *
    * @param workflowEvent
    */
  override def handle(workflowEvent: WorkflowEvent): Unit = {}
}