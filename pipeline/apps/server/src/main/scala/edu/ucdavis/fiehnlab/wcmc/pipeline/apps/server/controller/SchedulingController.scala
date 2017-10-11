package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.controller

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.{AdvancedTaskScheduler, Task, TaskScheduler}
import edu.ucdavis.fiehnlab.wcmc.utilities.casetojson.config.CaseClassToJSONSerializationAutoConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Configuration, Import}
import org.springframework.web.bind.annotation._

import scala.collection.JavaConverters._

/**
  * This provides the REST interface to the CARROT processing system
  * and will require a bean of the type taskScheduler to ensure
  * it's successful operation
  */
@CrossOrigin
@RestController
@RequestMapping(value = Array("/rest/schedule"))
class SchedulingController extends LazyLogging {

  /**
    * runs this provided task
    *
    * @param task
    */
  @RequestMapping(value = Array("/submit"), method = Array(RequestMethod.POST))
  def submit(@RequestBody task: Task): Map[String, Any] = Map("result" -> taskScheduler.submit(task))

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


  /**
    * the actual used implementation of the scheduler
    */
  @Autowired
  val taskScheduler: TaskScheduler = null

}


@Configuration
@Import(Array(classOf[CaseClassToJSONSerializationAutoConfiguration]))
class SchedulingControllerConfig {

}