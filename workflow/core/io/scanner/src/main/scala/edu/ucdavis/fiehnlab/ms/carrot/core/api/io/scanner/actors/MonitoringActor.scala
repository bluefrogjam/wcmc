package edu.ucdavis.fiehnlab.ms.carrot.core.api.io.scanner.actors

import java.io.Writer

import akka.actor.Actor
import akka.actor.Actor.Receive
import org.apache.log4j.Logger

/**
  * utilized to shutdown the system, based on simple job counting
  */
class MonitoringActor extends Actor{
  val logger:Logger = Logger.getLogger(getClass)
  var tasks:Int = 0
  var processedTasks:Int = 0

  val begin = System.currentTimeMillis()

  /**
    * if we are out of tasks, we shutdown the system and print out some statistics
 *
    * @return
    */
  override def receive: Receive = {
    case x:TaskScheduled =>
      tasks = tasks + 1
    case x:TaskFinished =>
      tasks = tasks - 1
      processedTasks = processedTasks + 1

      //shutdown
      if(tasks == 0){
        val duration = System.currentTimeMillis() - begin

        logger.info("duration in ms: " + duration + " average time for task is " + duration.toDouble/processedTasks.toDouble + "ms and we had " + processedTasks + " tasks todo")
        context.system.shutdown()
      }
      else if(processedTasks % 100 == 0){
        logger.info(s"processed tasks: ${processedTasks}, average time is ${(System.currentTimeMillis() - begin).toDouble/processedTasks.toDouble }")
      }
  }
}

case class TaskScheduled()
case class TaskFinished()