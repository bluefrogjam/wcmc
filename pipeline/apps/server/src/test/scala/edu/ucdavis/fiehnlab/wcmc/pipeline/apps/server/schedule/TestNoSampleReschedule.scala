package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.schedule

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.storage.{SampleToProcess, Task}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.PositiveMode
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod, Matrix}
import edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.Carrot
import edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.controller.TestTaskScheduler
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.web.client.RestTemplate

@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Array(classOf[Carrot]))
class TestNoSampleReschedule extends WordSpec with ShouldMatchers with LazyLogging {
  @LocalServerPort
  private val port: Int = 0

  @Autowired
  val template: RestTemplate = null

  @Autowired
  val testScheduler: TestTaskScheduler = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Scheduler" should {
    "avoid reschedule of failed sample" in {

      val task = Task("test", "test@test.de", AcquisitionMethod(ChromatographicMethod("missing", None, None,Some(PositiveMode()))), Seq(SampleToProcess("test", "test", "test", "test", Matrix("test", "test", "test", Seq.empty))))

      val result: ResponseEntity[Map[String, String]] = template.postForEntity(s"http://localhost:${port}/rest/schedule/submit", task, classOf[Map[String, String]])

      val resData = result.getBody.get("result").get
      resData shouldBe "test"

      testScheduler.submittedTask should not be null

      while (!testScheduler.isFinished(resData)) {
        logger.debug("Waiting for processing...")
        Thread.sleep(5000)
      }


      logger.debug(testScheduler.queue.mkString(";"))
      logger.debug(testScheduler.isFailed(resData).toString)
    }
  }
}


//edu.ucdavis.fiehnlab.ms.carrot.core.workflow.sample.correction.GCMSTargetRetentionIndexCorrectionProcessTest
