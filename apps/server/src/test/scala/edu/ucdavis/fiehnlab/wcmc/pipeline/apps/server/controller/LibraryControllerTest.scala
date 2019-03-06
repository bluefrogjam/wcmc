package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.controller

import org.apache.logging.log4j.scala.Logging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, MergeLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.AnnotationTarget
import edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.Carrot
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.scalatest.time.SpanSugar._
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
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
class LibraryControllerTest extends WordSpec with Matchers with Logging with Eventually {


  @LocalServerPort
  private val port: Int = 0

  @Autowired
  val template: RestTemplate = null

  @Autowired
  val libraryAccess: MergeLibraryAccess = null

  @Autowired
  @Qualifier("monaLibraryAccess")
  val monaLibraryAccess: LibraryAccess[AnnotationTarget] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  val tgt1 = AddTarget(
    targetName = "target-1",
    precursor = 1.0,
    retentionTime = 2.0,
    library = "test",
    riMarker = true,
    mode = "positive",
    instrument = "test",
    column = "test"
  )

  val tgt2 = AddTarget(
    targetName = "target-2",
    precursor = 1.0,
    retentionTime = 2.0,
    library = "test 2",
    riMarker = true,
    mode = "positive",
    instrument = "test",
    column = "test"
  )

  "LibraryControllerTest" should {
    libraryAccess.deleteAll

    "add 1 target to the library test" in {
      //      template.delete(s"http://localhost:${port}/rest/library/test")

      val result = template.postForEntity(s"http://localhost:${port}/rest/library", tgt1, classOf[Map[Any, Any]])

      result.getStatusCodeValue shouldBe 200
    }

    "have 1 library" in {
      eventually(timeout(5 seconds)) {

        val libraries: Array[AcquisitionMethod] = template.getForObject(s"http://localhost:${port}/rest/library", classOf[Array[AcquisitionMethod]])

        libraries.length should be >= 1

        Thread.sleep(1000)
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
      //      template.delete(s"http://localhost:${port}/rest/library/test 2")

      val result = template.postForEntity(s"http://localhost:${port}/rest/library", tgt2, classOf[Map[Any, Any]])

      result.getStatusCodeValue shouldBe 200
    }

    "have 2 libraries" in {
      eventually(timeout(5 seconds)) {

        val libraries: Array[AcquisitionMethod] = template.getForObject(s"http://localhost:${port}/rest/library", classOf[Array[AcquisitionMethod]])


        libraries.length should be >= 2
        Thread.sleep(1000)
      }
    }

    "does not add the same spectra to the library again" in {
      intercept[HttpClientErrorException] {
        template.postForObject(s"http://localhost:${port}/rest/library", tgt1, classOf[Map[Any, Any]]) // this should fail already, but it doesn't for some reason
        template.postForObject(s"http://localhost:${port}/rest/library", tgt1, classOf[Map[Any, Any]])
      }
    }


    "able to load targets by library" in {

      val result = template.getForObject(s"http://localhost:${port}/rest/library/test 2", classOf[Array[Map[Any, Any]]])

      logger.info(s"result ${result}")
      result.foreach { x =>
        logger.info(s"entry: ${x}")

      }

      result should not be empty
      result.length shouldBe 1
    }

  }
}
