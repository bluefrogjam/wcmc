package edu.ucdavis.fiehnlab.ms.carrot.core.io

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@SpringBootTest(classes = Array(classOf[SampleLoaderTestConfig]))
@ActiveProfiles(Array("file.source.eclipse", "file.source.eclipse"))
class WindowsBugWhichDoesNotAllowToOpenFilesTwiceBugTest extends WordSpec {

  @Autowired
  val sampleLoader: SampleLoader = null

  val context = new TestContextManager(this.getClass)
  context.prepareTestInstance(this)

  "WindowsBugWhichDoesNotAllowToOpenFilesTwiceBugTest" must {

    "support mzml" should {

      1 to 10 foreach { run =>

        s"load data attempts - $run" in {
          if (System.getProperties.getProperty("os.name").toLowerCase() == "linux")
            true
          else {
            val delegate: Sample = sampleLoader.loadSample("test.mzXML").get
            assert(delegate.spectra.size == 1)
          }
        }
      }
    }
  }
}
