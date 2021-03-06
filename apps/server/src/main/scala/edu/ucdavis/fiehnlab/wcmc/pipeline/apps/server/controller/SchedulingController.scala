package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.controller

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.Task
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.{AdvancedTaskScheduler, TaskScheduler}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation._
import org.springframework.web.client.HttpClientErrorException

import scala.collection.JavaConverters._

/**
  * This provides the REST interface to the CARROT processing system
  * and will require a bean of the type taskScheduler to ensure
  * it's successful operation
  */
@CrossOrigin
@RestController
@RequestMapping(value = Array("/rest/schedule"))
class SchedulingController extends Logging {

  /**
    * the actual used implementation of the scheduler
    */
  @Autowired
  val taskScheduler: TaskScheduler = null

  /**
    * runs this provided task
    *
    * @param task
    */
  @RequestMapping(value = Array("/submit"), method = Array(RequestMethod.POST))
  def submit(@RequestBody task: Task): Map[String, Any] = {
    try {
      Map("result" -> taskScheduler.submit(task))
    } catch {
      case ex: HttpClientErrorException =>
        logger.error(ex.getMessage)
        Map("result" -> ex.getMessage)
    }
  }

  /**
    * the task has finished
    *
    * @param id
    * @return
    */
  @RequestMapping(path = Array("/finished/{id}"), method = Array(RequestMethod.GET))
  def isFinished(@PathVariable("id") id: String): Map[String, Any] = taskScheduler match {
    case scheduler: AdvancedTaskScheduler =>
      Map("result" -> scheduler.isFinished(id))
    case _ =>
      Map("result" -> "not supported")
  }

  /**
    * the task has failed
    *
    * @param id
    * @return
    */
  @RequestMapping(path = Array("/failed/{id}"), method = Array(RequestMethod.GET))
  def isFailed(@PathVariable("id") id: String): Map[String, Any] =
    taskScheduler match {
      case scheduler: AdvancedTaskScheduler =>
        Map("result" -> scheduler.isFailed(id))
      case _ =>
        Map("result" -> "not supported")
    }

  /**
    * the task has been scheduled
    *
    * @param id
    * @return
    */
  @RequestMapping(path = Array("/scheduled/{id}"), method = Array(RequestMethod.GET))
  def isScheduled(@PathVariable("id") id: String): Map[String, Any] = taskScheduler match {
    case scheduler: AdvancedTaskScheduler =>
      Map("result" -> scheduler.isScheduled(id))
    case _ =>
      Map("result" -> "not supported")
  }

  /**
    * the task is currently running
    *
    * @param id
    * @return
    */
  @RequestMapping(path = Array("/running/{id}"), method = Array(RequestMethod.GET))
  def isRunning(@PathVariable("id") id: String): Map[String, Any] = taskScheduler match {
    case scheduler: AdvancedTaskScheduler =>
      Map("result" -> scheduler.isRunning(id))
    case _ =>
      Map("result" -> "not supported")
  }

  /**
    * returns the current queue of the scheduler
    *
    * @return
    */
  @RequestMapping(path = Array("/queue"), method = Array(RequestMethod.GET))
  def queue: java.util.List[String] =
    taskScheduler match {
      case scheduler: AdvancedTaskScheduler =>
        scheduler.queue.asJava
      case _ =>
        List.empty[String].asJava
    }

}


@Configuration
class SchedulingControllerConfig {

}
