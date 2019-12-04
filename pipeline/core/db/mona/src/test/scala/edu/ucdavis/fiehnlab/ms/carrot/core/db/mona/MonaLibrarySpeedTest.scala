package edu.ucdavis.fiehnlab.ms.carrot.core.db.mona

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.SampleLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, Ion, NegativeMode, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.apache.logging.log4j.scala.Logging
import org.scalatest.concurrent.Eventually
import org.scalatest.time.SpanSugar._
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlgemuth on 8/14/17.
  */

@SpringBootTest
@ActiveProfiles(Array("carrot.targets.mona", "test", "file.source.eclipse"))
class MonaLibrarySpeedTest extends WordSpec with Matchers with Logging with Eventually with BeforeAndAfterEach {

  @Autowired
  val library: MonaLibraryAccess = null

  @Autowired
  val client: MonaSpectrumRestClient = null

  @Autowired
  val loader: SampleLoader = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  val mzRt = new AnnotationTarget {
    override val uniqueMass: Option[Double] = None
    override var name: Option[String] = Some("1_MAG (17:0/0:0/0:0) iSTD [M+HAc-H]- _SVUQHVRAGMNPLW-UHFFFAOYSA-N")
    override val retentionIndex: Double = 188.1
    override var inchiKey: Option[String] = None
    override val precursorMass: Option[Double] = Option(403.3066)
    override var confirmed: Boolean = true
    override var requiredForCorrection: Boolean = false
    override var isRetentionIndexStandard: Boolean = true
    override val spectrum: Option[SpectrumProperties] = Some(new SpectrumProperties {
      override val msLevel: Short = 1
      override val modelIons: Option[Seq[Double]] = None
      override val ions: Seq[Ion] = Seq(Ion(403.3066, 100))
      override val rawIons: Option[Seq[Ion]] = None
    })
  }

  val acquisitionMethod1: AcquisitionMethod = AcquisitionMethod(ChromatographicMethod("test", None, None, Some(PositiveMode())))
  val acquisitionMethod2: AcquisitionMethod = AcquisitionMethod(ChromatographicMethod("test", None, None, Some(NegativeMode())))
  val acquisitionMethod3: AcquisitionMethod = AcquisitionMethod(ChromatographicMethod("keim", Some("6550"), Some("test"), Some(PositiveMode())))


  "MonaLibraryAccessTest" should {

    "be able to add an mzrt target" in {
      library.deleteAll

      eventually(timeout(10 seconds), interval(1 second)) {
        val initargets = library.load(acquisitionMethod3)

        initargets shouldBe empty
      }

      val begin = System.currentTimeMillis()
      val count = 2000
      for (i <- 1 to count) {
        library.add(mzRt, acquisitionMethod3, None)

      }

      val end = System.currentTimeMillis()

      val duration = end - begin
      val avg = duration / count

      logger.info(s"average time to add a spectra was ${avg}. Estimated over ${count} inserts into mona, which took ${duration / 1000} s in total")
      assert(avg < 200)

      eventually(timeout(10 seconds), interval(1 second)) {
        val targets = library.load(acquisitionMethod3)

        targets.size shouldBe count
      }


      library.deleteLibrary(acquisitionMethod3)
    }

  }

  override protected def afterEach(): Unit = Thread.sleep(5000)
}

