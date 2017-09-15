package edu.ucdavis.fiehnlab.wcmc.schedule.server

import javax.xml.transform.Result

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.experiment.Experiment
import edu.ucdavis.fiehnlab.wcmc.schedule.api._
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.annotation.Bean
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate

/**
  * Created by wohlgemuth on 8/28/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SchedulingControllerTest extends WordSpec with ShouldMatchers {

  @LocalServerPort
  private val port: Int = 0

  @Autowired
  val template: RestTemplate = null

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "SchedulingControllerTest" should {
    "support the following " must {

      "queue" in {

        val result = template.getForObject(s"http://localhost:${port}/rest/schedule/queue", classOf[Array[String]])

        result.size shouldBe 1
      }

      "submit" in {
        val task = Task("test", "test@test.de",AcquisitionMethod(None), Array.empty[SampleToProcess])

        val result:ResponseEntity[Map[String,String]] = template.postForEntity(s"http://localhost:${port}/rest/schedule/submit", task, classOf[Map[String,String]])

        result.getBody.get("result").get shouldBe "test"
      }

      "isFailed" in {

        template.getForObject(s"http://localhost:${port}/rest/schedule/failed/TaskA", classOf[Map[String,Any]]).get("result").get shouldBe true
        template.getForObject(s"http://localhost:${port}/rest/schedule/failed/TaskCB", classOf[Map[String,Any]]).get("result").get shouldBe false


      }

      "isScheduled" in {

        template.getForObject(s"http://localhost:${port}/rest/schedule/scheduled/TaskB", classOf[Map[String,Any]]).get("result").get shouldBe true
        template.getForObject(s"http://localhost:${port}/rest/schedule/scheduled/TaskCB", classOf[Map[String,Any]]).get("result").get shouldBe false

      }

      "isFinished" in {

        template.getForObject(s"http://localhost:${port}/rest/schedule/finished/TaskC", classOf[Map[String,Any]]).get("result").get shouldBe true
        template.getForObject(s"http://localhost:${port}/rest/schedule/finished/TaskCB", classOf[Map[String,Any]]).get("result").get shouldBe false

      }

      "isRunning" in {

        template.getForObject(s"http://localhost:${port}/rest/schedule/running/TaskD", classOf[Map[String,Any]]).get("result").get shouldBe true
        template.getForObject(s"http://localhost:${port}/rest/schedule/running/TaskCB", classOf[Map[String,Any]]).get("result").get shouldBe false

      }

    }
  }
}

@Component
class TestStorage extends ResultStorage{
  /**
    * store the given experiment
    *
    * @param experiment
    */
  override def store(experiment: Experiment, task: Task): Unit = ???
}

@Component
class TestTaskScheduler extends AdvancedTaskScheduler{
  /**
    * runs this provided task
    *
    * @param task
    */
  override def submit(task: Task): String = {
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
@SpringBootApplication
@EnableAutoConfiguration(exclude = Array(classOf[DataSourceAutoConfiguration]))
class SchedulingControllerConfigTest {

}