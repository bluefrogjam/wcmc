package edu.ucdavis.fiehnlab.wcmc.schedule.server

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.wcmc.schedule.api.{Task, TaskScheduler}
import edu.ucdavis.fiehnlab.wcms.utilities.casetojson.config.CaseClassToJSONSerializationConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Configuration, Import}
import org.springframework.web.bind.annotation._
import scala.collection.JavaConverters._

/**
  * This provides the REST interface to the CARROT processing system
  * and will require a bean of the type taskScheduler to ensure
  * it's successful operation
  */
@RestController
@RequestMapping(value = Array("/rest"))
class SchedulingController extends LazyLogging {

  /**
    * runs this provided task
    *
    * @param task
    */
  @RequestMapping(value = Array("/submit"), method = Array(RequestMethod.POST))
  def submit(@RequestBody task: Task): Map[String,Any] = Map("result" -> taskScheduler.submit(task))

  /**
    * the task has finished
    *
    * @param id
    * @return
    */
  @RequestMapping(path = Array("/finished/{id}"), method = Array(RequestMethod.GET))
  def isFinished(@PathVariable("id")id: String): Map[String,Any] = Map("result" -> taskScheduler.isFinished(id))

  /**
    * the task has failed
    *
    * @param id
    * @return
    */
  @RequestMapping(path = Array("/failed/{id}"), method = Array(RequestMethod.GET))
  def isFailed(@PathVariable("id")id: String): Map[String,Any] = Map("result" -> taskScheduler.isFailed(id))

  /**
    * the task has been scheduled
    *
    * @param id
    * @return
    */
  @RequestMapping(path = Array("/scheduled/{id}"), method = Array(RequestMethod.GET))
  def isScheduled(@PathVariable("id")id: String): Map[String,Any] = Map("result" -> taskScheduler.isScheduled(id))

  /**
    * the task is currently running
    *
    * @param id
    * @return
    */
  @RequestMapping(path = Array("/running/{id}"), method = Array(RequestMethod.GET))
  def isRunning(@PathVariable("id") id: String): Map[String,Any] = Map("result" -> taskScheduler.isRunning(id))

  /**
    * returns the current queue of the scheduler
    *
    * @return
    */
  @RequestMapping(path = Array("/queue"), method = Array(RequestMethod.GET))
  def queue: java.util.List[String] = taskScheduler.queue.asJava

  /**
    * the actual used implementation of the scheduler
    */
  @Autowired
  val taskScheduler: TaskScheduler = null

}



@Configuration
@Import(Array(classOf[CaseClassToJSONSerializationConfiguration]))
class SchedulingControllerConfig{

}