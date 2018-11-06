package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.event

import com.typesafe.scalalogging.LazyLogging
import org.springframework.stereotype.Component

@Component
case class MemoryMonitorEventListener() extends WorkflowEventListener with LazyLogging {
  /**
    * handles the MemoryMonitorEvent events
    *
    * @param event
    */
  override def handle(event: WorkflowEvent): Unit = {
    event match {
      case ev: MemoryMonitorEvent =>
        val runtime = Runtime.getRuntime

        logger.info(s"\tMemory status after ${ev.stage.getOrElse()} (in Mb)")
        logger.info("\tStage\tMax\tTotal\tFree\tUsed")
        logger.info(s"\t${ev.stage.get}\t${runtime.maxMemory() / (1024 ^ 2)}" +
            s"\t${runtime.totalMemory() / (1024 ^ 2)}" +
            s"\t${runtime.freeMemory() / (1024 ^ 2)}" +
            s"\t${(runtime.totalMemory() - runtime.freeMemory()) / (1024 ^ 2)}")
      case _ => None
    }
  }
}
