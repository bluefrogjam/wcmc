package edu.ucdavis.fiehnlab.ms.carrot.core.db.mona

import edu.ucdavis.fiehnlab.loader.DelegatingResourceLoader
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, Ion, NegativeMode, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import edu.ucdavis.fiehnlab.ms.carrot.core.io.ResourceLoaderSampleLoader
import org.apache.logging.log4j.scala.Logging
import org.scalatest.concurrent.Eventually
import org.scalatest.time.SpanSugar._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Import}
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
 * Created by wohlgemuth on 8/14/17.
 */

@SpringBootTest
@ActiveProfiles(Array("carrot.targets.mona", "test"))
class MonaLibraryAccessTest extends WordSpec with Matchers with Logging with Eventually with BeforeAndAfterEach with BeforeAndAfterAll {

  @Autowired
  val library: MonaLibraryAccess = null

  @Autowired
  val client: MonaSpectrumRestClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)


  val testTarget = new AnnotationTarget {
    override var name: Option[String] = None
    override val retentionIndex: Double = 100
    override var inchiKey: Option[String] = Option("UDOOPSJCRMKSGL-ZHACJKMWSA-N")
    override val precursorMass: Option[Double] = Option(224.0837)
    override var confirmed: Boolean = false
    override var requiredForCorrection: Boolean = true
    override var isRetentionIndexStandard: Boolean = true
    override val spectrum = Option(new SpectrumProperties {
      override val modelIons: Option[Seq[Double]] = None
      override val ions: Seq[Ion] = Seq(Ion(100.021, 123.123f), Ion(224.0837, 1231.021f))
      override val rawIons: Option[Seq[Ion]] = None
      override val msLevel: Short = 2
    })
    override val uniqueMass: Option[Double] = None
  }
  val testTarget2 = new AnnotationTarget {
    override val uniqueMass: Option[Double] = None
    override var name: Option[String] = None
    override val retentionIndex: Double = 100.5
    override var inchiKey: Option[String] = Option("UDOOPSJCRMKSGL-ZHACJKMWSA-N")
    override val precursorMass: Option[Double] = Option(224.0831)
    override var confirmed: Boolean = false
    override var requiredForCorrection: Boolean = true
    override var isRetentionIndexStandard: Boolean = true
    override val spectrum = Option(new SpectrumProperties {
      override val modelIons: Option[Seq[Double]] = None
      override val ions: Seq[Ion] = Seq(Ion(100.021, 123.123f), Ion(224.0837, 1231.021f))
      override val rawIons: Option[Seq[Ion]] = None
      override val msLevel: Short = 2

    })
  }
  val mzRt = new AnnotationTarget {
    override val uniqueMass: Option[Double] = None
    override var name: Option[String] = Some("1_MAG (17:0/0:0/0:0) iSTD [M+HAc-H]- _SVUQHVRAGMNPLW-UHFFFAOYSA-N")
    override val retentionIndex: Double = 188.1
    override var inchiKey: Option[String] = None
    override val precursorMass: Option[Double] = Option(403.3066)
    override var confirmed: Boolean = true
    override var requiredForCorrection: Boolean = false
    override var isRetentionIndexStandard: Boolean = true
    override val spectrum: Option[SpectrumProperties] = None
  }

  val acquisitionMethod1: AcquisitionMethod = AcquisitionMethod(ChromatographicMethod("test", None, None, Some(PositiveMode())))
  val acquisitionMethod2: AcquisitionMethod = AcquisitionMethod(ChromatographicMethod("test", None, None, Some(NegativeMode())))
  val acquisitionMethod3: AcquisitionMethod = AcquisitionMethod(ChromatographicMethod("keim", Some("6530"), Some("test"), Some(PositiveMode())))

  override def beforeAll() {
    library.deleteLibrary(acquisitionMethod1)
    library.deleteLibrary(acquisitionMethod2)
    library.deleteLibrary(acquisitionMethod3)
    client.regenerateStatistics()
  }

  override def afterAll() {
    library.deleteLibrary(acquisitionMethod1)
    library.deleteLibrary(acquisitionMethod2)
    library.deleteLibrary(acquisitionMethod3)
    client.regenerateStatistics()
  }

  "MonaLibraryAccessTest" should {
    "query only confirmed targets" in {
      library.query(acquisitionMethod1, confirmed = true) should equal("""(tags.text=="test - unknown - unknown - positive") and (metaData=q='name=="confirmed" and value=="true"')""")
    }

    "query only unconfirmed targets" in {
      library.query(acquisitionMethod1, confirmed = false) should equal("""(tags.text=="test - unknown - unknown - positive") and (metaData=q='name=="confirmed" and value=="false"')""")
    }

    "be able to add an mzrt target" in {
      library.deleteLibrary(acquisitionMethod3)

      eventually(timeout(5 seconds), interval(1 second)) {
        val initargets = library.load(acquisitionMethod3)

        initargets shouldBe empty
      }

      library.add(Seq(mzRt), acquisitionMethod3, None)
      eventually(timeout(5 seconds), interval(1 second)) {
        val targets = library.load(acquisitionMethod3)

        targets.size shouldBe 1
      }

      library.deleteLibrary(acquisitionMethod3)
    }

    "reset database using mona client" in {
      logger.info(s"deleting mona spectra using mona client")
      client.list().foreach { x =>
        client.delete(x.id)
      }
      client.regenerateStatistics
      try {
        client.regenerateDownloads
      }
      catch {
        case e: Exception =>
          logger.warn("Persistence server doesn't exist")
      }

      eventually(timeout(5 seconds), interval(1 second)) {
        client.list().size shouldBe 0
        library.load(acquisitionMethod1) should have size 0
      }
    }


    "be possible to add and load targets" in {
      val confirmed = Some(false)

      library.add(Seq(testTarget), acquisitionMethod1, None)
      eventually(timeout(5 seconds), interval(1 second)) {
        library.load(acquisitionMethod1, confirmed).size shouldBe 1
      }

      library.add(Seq(testTarget2), acquisitionMethod1, None)
      eventually(timeout(5 seconds), interval(1 second)) {
        library.load(acquisitionMethod1, confirmed).size shouldBe 2
      }

    }

    "be possible to add and load targets from a different library" in {
      val confirmed = Some(false)
      library.deleteLibrary(acquisitionMethod1, confirmed)
      library.deleteLibrary(acquisitionMethod2, confirmed)

      library.add(Seq(testTarget), acquisitionMethod1, None)
      eventually(timeout(5 seconds), interval(1 second)) {
        library.load(acquisitionMethod1, Some(false)).size shouldBe 1
      }

      library.add(Seq(testTarget2), acquisitionMethod2, None)
      eventually(timeout(5 seconds), interval(1 second)) {
        library.load(acquisitionMethod2, confirmed).size shouldBe 1
      }

      eventually(timeout(5 seconds), interval(1 second)) {
        val count: Int = library.libraries.map { x =>
          library.load(x, Some(false)).map { y =>
            1
          }.sum
        }.sum

        count shouldBe 2
      }
    }

    "be 2 acquisition methods defined now" in {
      library.deleteLibrary(acquisitionMethod1, Some(false))
      library.deleteLibrary(acquisitionMethod2, Some(false))

      library.add(Seq(testTarget), acquisitionMethod1, None)
      library.add(Seq(testTarget), acquisitionMethod2, None)
      eventually(timeout(5 seconds), interval(1 second)) {
        library.libraries.size shouldBe 2
      }

      library.deleteLibrary(acquisitionMethod1, Some(false))
      library.deleteLibrary(acquisitionMethod2, Some(false))
    }

    "be able to update the name of a spectrum" in {
      val confirmed = Some(false)

      library.deleteLibrary(acquisitionMethod1, confirmed)

      library.add(Seq(testTarget), acquisitionMethod1, None)
      eventually(timeout(5 seconds), interval(1 second)) {
        library.load(acquisitionMethod1, confirmed).size shouldBe 1
      }

      val target = library.load(acquisitionMethod1, confirmed).head
      target.name = Some("12345")

      library.update(target, acquisitionMethod1)
      eventually(timeout(5 seconds), interval(1 second)) {
        val updatedSpectra = library.load(acquisitionMethod1, confirmed).head
        updatedSpectra.name.get shouldBe ("12345")
      }
    }


    "be able to update the inchi key of a spectrum" in {
      val confirmed = Some(false)

      library.deleteLibrary(acquisitionMethod1, confirmed)

      library.add(Seq(testTarget), acquisitionMethod1, None)
      eventually(timeout(5 seconds), interval(1 second)) {
        library.load(acquisitionMethod1, confirmed).size shouldBe 1
      }

      val target = library.load(acquisitionMethod1, confirmed).head
      println(s"first $target")
      target.inchiKey = Option("QNAYBMKLOCPYGJ-REOHCLBHSA-N")

      library.update(target, acquisitionMethod1)

      eventually(timeout(5 seconds), interval(1 second)) {
        val updatedSpectra = library.load(acquisitionMethod1, confirmed).head
        updatedSpectra.inchiKey.get shouldBe ("QNAYBMKLOCPYGJ-REOHCLBHSA-N")
      }

    }

    "be able to update the confirmed status a spectrum to true" in {
      val confirmed = Some(false)

      library.deleteLibrary(acquisitionMethod1)
      library.deleteLibrary(acquisitionMethod1, confirmed)

      library.add(Seq(testTarget), acquisitionMethod1, None)
      eventually(timeout(5 seconds), interval(1 second)) {
        library.load(acquisitionMethod1, confirmed).size shouldBe 1
      }

      val target = library.load(acquisitionMethod1, confirmed).head
      target.confirmed = true

      library.update(target, acquisitionMethod1)
      eventually(timeout(5 seconds), interval(1 second)) {
        val updatedSpectra = library.load(acquisitionMethod1).head
        updatedSpectra.confirmed shouldBe true
      }

      library.deleteLibrary(acquisitionMethod1)
      library.deleteLibrary(acquisitionMethod1, confirmed)
    }

    "be able to update the confirmed status a spectrum to false" in {
      val confirmed = Some(false)

      library.deleteLibrary(acquisitionMethod1)
      library.deleteLibrary(acquisitionMethod1, confirmed)

      library.add(Seq(mzRt), acquisitionMethod1, None)

      eventually(timeout(value = 5 seconds), interval(1 second)) {
        val target = library.load(acquisitionMethod1)
        target should not be empty

        target.head.confirmed = false
        library.update(target.head, acquisitionMethod1)
      }

      eventually(timeout(7 seconds), interval(1 second)) {
        val updatedSpectra = library.load(acquisitionMethod1, confirmed).head
        // load the newly unconfirmed target
        updatedSpectra.confirmed shouldBe false
      }

      library.deleteLibrary(acquisitionMethod1, confirmed)
    }


    "be able to update the retention index status of a spectrum to false" in {

      library.deleteLibrary(acquisitionMethod1)

      library.add(Seq(mzRt), acquisitionMethod1, None)
      eventually(timeout(5 seconds), interval(1 second)) {
        library.load(acquisitionMethod1).size shouldBe 1
      }

      val target = library.load(acquisitionMethod1).head
      target.isRetentionIndexStandard = true

      library.update(target, acquisitionMethod1)
      eventually(timeout(5 seconds), interval(1 second)) {
        val updatedSpectra = library.load(acquisitionMethod1).head
        updatedSpectra.isRetentionIndexStandard shouldBe true
      }

    }

    "be able to update the retention index status of a spectrum to true" in {

      library.deleteLibrary(acquisitionMethod1)

      library.add(Seq(mzRt), acquisitionMethod1, None)

      eventually(timeout(5 seconds), interval(1 second)) {
        library.load(acquisitionMethod1).size shouldBe 1
      }

      val target = library.load(acquisitionMethod1).head
      target.isRetentionIndexStandard = true

      library.update(target, acquisitionMethod1)
      eventually(timeout(5 seconds), interval(1 second)) {
        val updatedSpectra = library.load(acquisitionMethod1).head
        updatedSpectra.isRetentionIndexStandard shouldBe true
      }
    }

    "be able to update the retention index requiered status of a spectrum to true" in {
      val confirmed = Some(false)

      library.deleteLibrary(acquisitionMethod1, confirmed)

      library.add(Seq(testTarget), acquisitionMethod1, None)
      eventually(timeout(5 seconds), interval(1 second)) {
        library.load(acquisitionMethod1, confirmed).size shouldBe 1
      }

      val target = library.load(acquisitionMethod1, confirmed).head
      target.requiredForCorrection = true

      library.update(target, acquisitionMethod1)
      eventually(timeout(5 seconds), interval(1 second)) {
        val updatedSpectra = library.load(acquisitionMethod1, confirmed).head
        updatedSpectra.requiredForCorrection shouldBe true
      }

    }

    "be able to update the retention index required status of a spectrum to false" in {
      val confirmed = Some(false)

      library.deleteLibrary(acquisitionMethod1, confirmed)

      library.add(Seq(testTarget), acquisitionMethod1, None)
      eventually(timeout(5 seconds), interval(1 second)) {
        library.load(acquisitionMethod1, confirmed).size shouldBe 1
      }

      val target = library.load(acquisitionMethod1, confirmed).head
      target.requiredForCorrection = false

      library.update(target, acquisitionMethod1)
      eventually(timeout(5 seconds), interval(1 second)) {
        val updatedSpectra = library.load(acquisitionMethod1, confirmed).head
        updatedSpectra.requiredForCorrection shouldBe false
      }
    }
  }

  //  override protected def afterEach(): Unit = Thread.sleep(5000)
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[JWTAuthenticationConfig]))
class MonaLibraryAccessTestConfiguration {


  @Autowired
  val resourceLoader: DelegatingResourceLoader = null

  @Bean
  def loader(delegatingResourceLoader: DelegatingResourceLoader): ResourceLoaderSampleLoader = new ResourceLoaderSampleLoader(delegatingResourceLoader)
}
