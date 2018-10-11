package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.controller

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.MergeLibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.db.mona.MonaLibraryAccess
import edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.Carrot
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.scalatest.time.SpanSugar._
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.web.client.{HttpClientErrorException, RestTemplate}

/**
  * Created by wohlgemuth on 10/17/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Array(classOf[Carrot]))
@ActiveProfiles(Array("carrot.lcms", "test"))
class LibraryControllerTest extends WordSpec with ShouldMatchers with LazyLogging with Eventually {


  @LocalServerPort
  private val port: Int = 0

  @Autowired
  val template: RestTemplate = null

  @Autowired
  val libraryAccess: MergeLibraryAccess = null

  @Autowired
  val monaLibraryAccess: MonaLibraryAccess = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "LibraryControllerTest" should {

    "add 1 target to the library test" in {
      val result = template.postForObject(s"http://localhost:${port}/rest/library", AddTarget(
        targetName = "target-1",
        precursor = 1.0,
        retentionTime = 2.0,
        library = "test",
        riMarker = true,
        mode = "positive"
      ), classOf[Map[Any, Any]])

      result should not be null
      result("name") shouldBe "target-1"
    }

    "have 1 library" in {
      eventually(timeout(5 seconds)) {

        val libraries: Array[AcquisitionMethod] = template.getForObject(s"http://localhost:${port}/rest/library", classOf[Array[AcquisitionMethod]])

        libraries.length should be >= 1

        Thread.sleep(500)
      }
    }

    "added targed properties must be correct" in {

      val result = template.getForObject(s"http://localhost:${port}/rest/library/test", classOf[Array[Map[Any, Any]]])

      logger.info(s"result ${result.mkString("\n")}")
      result.foreach { x =>
        logger.info(s"entry: ${x.mkString("; ")}")
      }

      result.length should not be 0

    }

    "add 1 target to the library test 2" in {
      val result = template.postForObject(s"http://localhost:${port}/rest/library", AddTarget(
        targetName = "target-2",
        precursor = 1.0,
        retentionTime = 2.0,
        library = "test 2",
        riMarker = true,
        mode = "positive"
      ), classOf[Map[Any, Any]])

      result should not be null
      result("name") shouldBe "target-2"
    }

    "have 2 libraries" in {
      eventually(timeout(5 seconds)) {

        val libraries: Array[AcquisitionMethod] = template.getForObject(s"http://localhost:${port}/rest/library", classOf[Array[AcquisitionMethod]])


        libraries.length shouldBe 2
        Thread.sleep(500)
      }
    }

    "does not add the same spectra to the library again" in {

      val target = AddTarget(
        targetName = "target-2",
        precursor = 1.0,
        retentionTime = 2.0,
        library = "test 2",
        riMarker = true,
        mode = "positive"
      )

      intercept[HttpClientErrorException] {
        template.postForObject(s"http://localhost:${port}/rest/library", target, classOf[AddTarget])
      }
    }


    "able to load targets by library" in {

      val result = template.getForObject(s"http://localhost:${port}/rest/library/test 2", classOf[Array[Map[Any, Any]]])

      logger.info(s"result ${result}")
      result.foreach { x =>
        logger.info(s"entry: ${x}")

      }

      result.length should not be 0
    }

  }
}
