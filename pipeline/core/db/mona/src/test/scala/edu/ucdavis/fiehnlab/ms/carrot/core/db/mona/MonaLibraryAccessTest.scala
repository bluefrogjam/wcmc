package edu.ucdavis.fiehnlab.ms.carrot.core.db.mona

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.SpanSugar._
import org.scalatest.{BeforeAndAfterEach, ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by wohlgemuth on 8/14/17.
  */

@SpringBootTest
@ActiveProfiles(Array("carrot.targets.mona"))
class MonaLibraryAccessTest extends WordSpec with ShouldMatchers with LazyLogging with Eventually with BeforeAndAfterEach {
  val testTarget = new Target {
    /**
      * a name for this spectra
      */
    override var name: Option[String] = None
    /**
      * retention time in seconds of this target
      */
    override val retentionIndex: Double = 100
    /**
      * the unique inchi key for this spectra
      */
    override var inchiKey: Option[String] = Option("UDOOPSJCRMKSGL-ZHACJKMWSA-N")
    /**
      * the mono isotopic mass of this spectra
      */
    override val precursorMass: Option[Double] = Option(224.0837)
    /**
      * is this a confirmed target
      */
    override var confirmed: Boolean = false
    /**
      * is this target required for a successful retention index correction
      */
    override var requiredForCorrection: Boolean = true
    /**
      * is this a retention index correction standard
      */
    override var isRetentionIndexStandard: Boolean = true
    /**
      * associated spectrum propties if applicable
      */
    override val spectrum = Option(new SpectrumProperties {
      /**
        * a list of model ions used during the deconvolution
        */
      override val modelIons: Option[Seq[Double]] = None
      /**
        * all the defined ions for this spectra
        */
      override val ions: Seq[Ion] = Seq(Ion(100.021, 123.123), Ion(224.0837, 1231.021))
      /**
        * the msLevel of this spectra
        */
      override val msLevel: Short = 2
    })
  }
  val testTarget2 = new Target {
    /**
      * a name for this spectra
      */
    override var name: Option[String] = None
    /**
      * retention time in seconds of this target
      */
    override val retentionIndex: Double = 100.5
    /**
      * the unique inchi key for this spectra
      */
    override var inchiKey: Option[String] = Option("UDOOPSJCRMKSGL-ZHACJKMWSA-N")
    /**
      * the mono isotopic mass of this spectra
      */
    override val precursorMass: Option[Double] = Option(224.0831)
    /**
      * is this a confirmed target
      */
    override var confirmed: Boolean = false
    /**
      * is this target required for a successful retention index correction
      */
    override var requiredForCorrection: Boolean = true
    /**
      * is this a retention index correction standard
      */
    override var isRetentionIndexStandard: Boolean = true
    /**
      * associated spectrum propties if applicable
      */
    override val spectrum = Option(new SpectrumProperties {
      /**
        * a list of model ions used during the deconvolution
        */
      override val modelIons: Option[Seq[Double]] = None
      /**
        * all the defined ions for this spectra
        */
      override val ions: Seq[Ion] = Seq(Ion(100.021, 123.123), Ion(224.0837, 1231.021))

      override val msLevel: Short = 2

    })
  }


  @Autowired
  val library: MonaLibraryAccess = null

  @Autowired
  val client: MonaSpectrumRestClient = null


  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MonaLibraryAccessTest" should {


    "reset database using mona client" in {
      client.list().foreach { x =>
        client.delete(x.id)
      }
      client.regenerateStatistics
      client.regenerateDownloads

      eventually(timeout(5 seconds)) {
        client.list().size shouldBe 0
        Thread.sleep(1000)
      }
    }


    "be possible to add and load targets" in {

      val acquisitionMethod: AcquisitionMethod = AcquisitionMethod(None)
      library.add(testTarget, acquisitionMethod, None)

      eventually(timeout(5 seconds)) {
        library.load(acquisitionMethod).size shouldBe 1
        Thread.sleep(1000)
      }
      library.add(testTarget2, acquisitionMethod, None)

      eventually(timeout(5 seconds)) {
        library.load(acquisitionMethod).size shouldBe 2
        Thread.sleep(1000)
      }

    }

    "be possible to add and load targets from a different library" in {

      val acquisitionMethod: AcquisitionMethod = AcquisitionMethod(Option(ChromatographicMethod("test", None, None, None)))
      library.add(testTarget, acquisitionMethod, None)
      eventually(timeout(5 seconds)) {
        library.load(acquisitionMethod).size shouldBe 1
        Thread.sleep(1000)
      }
      library.add(testTarget2, acquisitionMethod, None)
      eventually(timeout(5 seconds)) {
        library.load(acquisitionMethod).size shouldBe 2
        Thread.sleep(1000)
      }

      eventually(timeout(5 seconds)) {
        library.load(AcquisitionMethod(None)).size shouldBe 2
        Thread.sleep(1000)
      }
      eventually(timeout(5 seconds)) {

        val count: Int = library.libraries.map { x =>
          library.load(x).map { y =>
            1
          }.sum
        }.sum

        count shouldBe 4

        Thread.sleep(1000)
      }
    }

    "there should be 2 acquisition methods defined now" in {
      eventually(timeout(15 seconds)) {
        library.libraries.size shouldBe 2
        Thread.sleep(1000)
      }
    }

    "able to update the name of a spectrum" in {

      library.deleteAll

      val acquisitionMethod: AcquisitionMethod = AcquisitionMethod(Option(ChromatographicMethod("test", None, None, None)))

      eventually(timeout(15 seconds)) {
        library.libraries.size shouldBe 0
        Thread.sleep(1000)
      }
      library.add(testTarget, acquisitionMethod, None)

      eventually(timeout(5 seconds)) {
        library.load(acquisitionMethod).size shouldBe 1
        Thread.sleep(1000)
      }

      val target = library.load(acquisitionMethod).head

      target.name = Option("12345")

      library.update(target, acquisitionMethod)

      eventually(timeout(15 seconds)) {
        val updatedSpectra = library.load(acquisitionMethod).head
        updatedSpectra.name.get shouldBe ("12345")
      }

    }


    "able to update the inchi key of a spectrum" in {

      library.deleteAll

      eventually(timeout(15 seconds)) {
        library.libraries.size shouldBe 0
        client.list().size shouldBe 0
        Thread.sleep(1000)
      }

      val acquisitionMethod: AcquisitionMethod = AcquisitionMethod(Option(ChromatographicMethod("test", None, None, None)))


      library.add(testTarget, acquisitionMethod, None)

      eventually(timeout(5 seconds)) {
        library.load(acquisitionMethod).size shouldBe 1
        Thread.sleep(1000)

      }


      val target = library.load(acquisitionMethod).head

      target.inchiKey = Option("QNAYBMKLOCPYGJ-REOHCLBHSA-N")

      library.update(target, acquisitionMethod)

      eventually(timeout(15 seconds)) {
        val updatedSpectra = library.load(acquisitionMethod).head
        updatedSpectra.inchiKey.get shouldBe ("QNAYBMKLOCPYGJ-REOHCLBHSA-N")
      }

    }

    "able to update the confirmed status a spectrum to true" in {

      library.deleteAll
      eventually(timeout(15 seconds)) {
        library.libraries.size shouldBe 0
        client.list().size shouldBe 0

        Thread.sleep(1000)
      }

      val acquisitionMethod: AcquisitionMethod = AcquisitionMethod(Option(ChromatographicMethod("test", None, None, None)))

      library.add(testTarget, acquisitionMethod, None)

      eventually(timeout(5 seconds)) {
        library.load(acquisitionMethod).size shouldBe 1
        Thread.sleep(1000)
      }

      val target = library.load(acquisitionMethod).head

      target.confirmed = true

      library.update(target, acquisitionMethod)

      eventually(timeout(15 seconds)) {
        val updatedSpectra = library.load(acquisitionMethod).head
        updatedSpectra.confirmed shouldBe true
        Thread.sleep(1000)

      }

    }

    "able to update the confirmed status a spectrum to false" in {

      library.deleteAll
      eventually(timeout(15 seconds)) {
        library.libraries.size shouldBe 0
        client.list().size shouldBe 0

        Thread.sleep(1000)
      }

      val acquisitionMethod: AcquisitionMethod = AcquisitionMethod(Option(ChromatographicMethod("test", None, None, None)))
      library.add(testTarget, acquisitionMethod, None)

      eventually(timeout(15 seconds)) {
        library.load(acquisitionMethod).size shouldBe 1
        Thread.sleep(1000)
      }

      val target = library.load(acquisitionMethod).head

      target.confirmed = false

      library.update(target, acquisitionMethod)

      eventually(timeout(15 seconds)) {
        val updatedSpectra = library.load(acquisitionMethod).head
        updatedSpectra.confirmed shouldBe false
        Thread.sleep(1000)

      }
    }


    "able to update the retention index status of a spectrum to false" in {

      library.deleteAll
      eventually(timeout(15 seconds)) {
        library.libraries.size shouldBe 0
        client.list().size shouldBe 0

        Thread.sleep(1000)
      }

      val acquisitionMethod: AcquisitionMethod = AcquisitionMethod(Option(ChromatographicMethod("test", None, None, None)))
      library.add(testTarget, acquisitionMethod, None)

      eventually(timeout(15 seconds)) {
        library.load(acquisitionMethod).size shouldBe 1
        Thread.sleep(1000)
      }

      val target = library.load(acquisitionMethod).head

      target.isRetentionIndexStandard = true

      library.update(target, acquisitionMethod)

      eventually(timeout(15 seconds)) {
        val updatedSpectra = library.load(acquisitionMethod).head
        updatedSpectra.isRetentionIndexStandard shouldBe true
        Thread.sleep(1000)

      }

    }

    "able to update the retention index status of a spectrum to true" in {

      library.deleteAll
      eventually(timeout(15 seconds)) {
        library.libraries.size shouldBe 0
        client.list().size shouldBe 0

        Thread.sleep(1000)
      }

      val acquisitionMethod: AcquisitionMethod = AcquisitionMethod(Option(ChromatographicMethod("test", None, None, None)))
      library.add(testTarget, acquisitionMethod, None)

      eventually(timeout(15 seconds)) {
        library.load(acquisitionMethod).size shouldBe 1
        Thread.sleep(1000)
      }

      val target = library.load(acquisitionMethod).head

      target.isRetentionIndexStandard = true

      library.update(target, acquisitionMethod)

      eventually(timeout(15 seconds)) {
        val updatedSpectra = library.load(acquisitionMethod).head
        updatedSpectra.isRetentionIndexStandard shouldBe true
        Thread.sleep(1000)

      }
    }

    "able to update the retention index requiered status of a spectrum to true" in {

      library.deleteAll
      eventually(timeout(15 seconds)) {
        library.libraries.size shouldBe 0
        client.list().size shouldBe 0

        Thread.sleep(1000)
      }

      val acquisitionMethod: AcquisitionMethod = AcquisitionMethod(Option(ChromatographicMethod("test", None, None, None)))
      library.add(testTarget, acquisitionMethod, None)

      eventually(timeout(15 seconds)) {
        library.load(acquisitionMethod).size shouldBe 1
        Thread.sleep(1000)
      }

      val target = library.load(acquisitionMethod).head

      target.requiredForCorrection = true

      library.update(target, acquisitionMethod)

      eventually(timeout(15 seconds)) {
        val updatedSpectra = library.load(acquisitionMethod).head
        updatedSpectra.requiredForCorrection shouldBe true
      }

    }

    "able to update the retention index required status of a spectrum to false" in {

      library.deleteAll
      eventually(timeout(15 seconds)) {
        library.libraries.size shouldBe 0
        client.list().size shouldBe 0

        Thread.sleep(1000)
      }

      val acquisitionMethod: AcquisitionMethod = AcquisitionMethod(Option(ChromatographicMethod("test", None, None, None)))
      library.add(testTarget, acquisitionMethod, None)

      eventually(timeout(15 seconds)) {
        library.load(acquisitionMethod).size shouldBe 1
        Thread.sleep(1000)
      }

      val target = library.load(acquisitionMethod).head

      target.requiredForCorrection = false

      library.update(target, acquisitionMethod)

      eventually(timeout(15 seconds)) {
        val updatedSpectra = library.load(acquisitionMethod).head
        updatedSpectra.requiredForCorrection shouldBe false
      }

    }
  }

  override protected def afterEach(): Unit = Thread.sleep(5000)
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[JWTAuthenticationConfig]))
class MonaLibraryAccessTestConfiguration {

}
