package edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.controller

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import edu.ucdavis.fiehnlab.wcmc.pipeline.apps.server.Carrot
import org.junit.runner.RunWith
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate

/**
  * Created by wohlgemuth on 10/17/17.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Array(classOf[Carrot]))
class LibraryControllerTest extends WordSpec with ShouldMatchers with LazyLogging {


  @LocalServerPort
  private val port: Int = 0

  @Autowired
  val template: RestTemplate = null

  @Autowired
  val libraryAccess: LibraryAccess[Target] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "LibraryControllerTest" should {

    "have no library" in {
      val libraries: List[String] = template.getForObject(s"http://localhost:${port}/rest/library", classOf[List[String]])

      libraries.size shouldBe 0

    }

    "add 1 target to the library test" in {
      template.postForObject(s"http://localhost:${port}/rest/library", AddTarget(
        targetName = "target-1",
        precursor = 1.0,
        retentionTime = 2.0,
        library = "test",
        riMarker = true,
        mode = "positive"
      ), classOf[Void])
    }

    "have 1 library" in {
      val libraries: List[String] = template.getForObject(s"http://localhost:${port}/rest/library", classOf[List[String]])

      libraries.size shouldBe 1

    }

    "add 1 target to the library test 2" in {
      template.postForObject(s"http://localhost:${port}/rest/library", AddTarget(
        targetName = "target-2",
        precursor = 1.0,
        retentionTime = 2.0,
        library = "test 2",
        riMarker = true,
        mode = "positive"
      ), classOf[Void])

    }

    "have 2 libraries" in {
      val libraries: List[String] = template.getForObject(s"http://localhost:${port}/rest/library", classOf[List[String]])

      libraries.size shouldBe 2

    }

  }
}
