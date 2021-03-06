package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.controller

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{ResultStorage, SampleToProcess, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{NegativeMode, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod, Matrix}
import edu.ucdavis.fiehnlab.ms.carrot.core.schedule.AdvancedTaskScheduler
import edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.Carrot
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Primary
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.web.client.RestTemplate

/**
  * Created by wohlgemuth on 8/28/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Array(classOf[Carrot]))
@ActiveProfiles(Array("test"))
class SchedulingControllerTest extends WordSpec with Matchers with Logging {

  @LocalServerPort
  private val port: Int = 0

  @Autowired
  val template: RestTemplate = null

  @Autowired
  val testScheduler: TestTaskScheduler = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "SchedulingControllerTest" should {
    "support the following " must {

      "queue" in {

        val url = s"http://localhost:${port}/rest/schedule/queue"

        logger.info(s"url: $url")

        val result = template.getForObject(url, classOf[Array[String]])

        result.size shouldBe 1
      }

      "submit with AcquisitionMethod(None)" in {
        val task = Task("test", "test@test.de", AcquisitionMethod(), Seq(SampleToProcess("test", "test", "test", "test", Matrix("test", "test", "test", Seq.empty))))

        val result: ResponseEntity[Map[String, String]] = template.postForEntity(s"http://localhost:${port}/rest/schedule/submit", task, classOf[Map[String, String]])

        result.getBody.get("result").get shouldBe "test"

        testScheduler.submittedTask should not be null

        testScheduler.submittedTask.acquisitionMethod should not be null

        testScheduler.submittedTask.acquisitionMethod shouldEqual (AcquisitionMethod())

      }

      "isFailed" in {

        template.getForObject(s"http://localhost:${port}/rest/schedule/failed/TaskA", classOf[Map[String, Any]]).get("result").get shouldBe true
        template.getForObject(s"http://localhost:${port}/rest/schedule/failed/TaskCB", classOf[Map[String, Any]]).get("result").get shouldBe false


      }

      "isScheduled" in {

        template.getForObject(s"http://localhost:${port}/rest/schedule/scheduled/TaskB", classOf[Map[String, Any]]).get("result").get shouldBe true
        template.getForObject(s"http://localhost:${port}/rest/schedule/scheduled/TaskCB", classOf[Map[String, Any]]).get("result").get shouldBe false

      }

      "isFinished" in {

        template.getForObject(s"http://localhost:${port}/rest/schedule/finished/TaskC", classOf[Map[String, Any]]).get("result").get shouldBe true
        template.getForObject(s"http://localhost:${port}/rest/schedule/finished/TaskCB", classOf[Map[String, Any]]).get("result").get shouldBe false

      }

      "isRunning" in {

        template.getForObject(s"http://localhost:${port}/rest/schedule/running/TaskD", classOf[Map[String, Any]]).get("result").get shouldBe true
        template.getForObject(s"http://localhost:${port}/rest/schedule/running/TaskCB", classOf[Map[String, Any]]).get("result").get shouldBe false

      }

      "submit with AcquisitionMethod(Some(...))" in {
        val task = Task("test", "binbase@gmail.com", AcquisitionMethod(ChromatographicMethod("test", None, None, None)), Seq(SampleToProcess("test", "test", "test", "test", Matrix("test", "test", "test", Seq.empty))))

        val result: ResponseEntity[Map[String, String]] = template.postForEntity(s"http://localhost:${port}/rest/schedule/submit", task, classOf[Map[String, String]])

        result.getBody.get("result").get shouldBe "test"

        testScheduler.submittedTask should not be null

        testScheduler.submittedTask.acquisitionMethod should not be null

        testScheduler.submittedTask.acquisitionMethod shouldEqual AcquisitionMethod(ChromatographicMethod("test", None, None, None))

      }

      "submit with AcquisitionMethod(Some(...)) and positive ion mode" in {
        val task = Task("test", "binbase@gmail.com", AcquisitionMethod(ChromatographicMethod("test", None, None, Option(PositiveMode()))), Seq(SampleToProcess("test", "test", "test", "test", Matrix("test", "test", "test", Seq.empty))))

        val result: ResponseEntity[Map[String, String]] = template.postForEntity(s"http://localhost:${port}/rest/schedule/submit", task, classOf[Map[String, String]])

        result.getBody.get("result").get shouldBe "test"

        testScheduler.submittedTask should not be null

        testScheduler.submittedTask.acquisitionMethod should not be null

        testScheduler.submittedTask.acquisitionMethod shouldEqual AcquisitionMethod(ChromatographicMethod("test", None, None, Option(PositiveMode())))

      }
      "submit with AcquisitionMethod(Some(...)) and negative ion mode" in {
        val task = Task("test", "binbase@gmail.com", AcquisitionMethod(ChromatographicMethod("test", None, None, Option(NegativeMode()))), Seq(SampleToProcess("test", "test", "test", "test", Matrix("test", "test", "test", Seq.empty))))

        val result: ResponseEntity[Map[String, String]] = template.postForEntity(s"http://localhost:${port}/rest/schedule/submit", task, classOf[Map[String, String]])

        result.getBody.get("result").get shouldBe "test"

        testScheduler.submittedTask should not be null

        testScheduler.submittedTask.acquisitionMethod should not be null

        testScheduler.submittedTask.acquisitionMethod shouldEqual AcquisitionMethod(ChromatographicMethod("test", None, None, Option(NegativeMode())))

      }

    }
  }
}


@Component
class TestStorage extends ResultStorage {
  /**
    * store the given experiment
    *
    * @param experiment
    */
  override def store(experiment: Experiment, task: Task): Unit = ???
}

@Component
@Primary
class TestTaskScheduler extends AdvancedTaskScheduler {

  var submittedTask: Task = _

  /**
    * runs this provided task
    *
    * @param task
    */
  override def doSubmit(task: Task): String = {
    submittedTask = task
    task.name
  }

  /**
    * the task has failed
    *
    * @param id
    * @return
    */
  override def isFailed(id: String): Boolean = id.toLowerCase().equals("taska")

  /**
    * the task has been scheduled
    *
    * @param id
    * @return
    */
  override def isScheduled(id: String): Boolean = id.toLowerCase.equals("taskb")

  /**
    * the task has finished
    *
    * @param id
    * @return
    */
  override def isFinished(id: String): Boolean = id.toLowerCase.equals("taskc")

  /**
    * the task is currently running
    *
    * @param id
    * @return
    */
  override def isRunning(id: String): Boolean = id.toLowerCase.equals("taskd")

  /**
    * returns the current queue of the scheduler
    *
    * @return
    */
  override def queue: Seq[String] = Seq("TaskA")
}
