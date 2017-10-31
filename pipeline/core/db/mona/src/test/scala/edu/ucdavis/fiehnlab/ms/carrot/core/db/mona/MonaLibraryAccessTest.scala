package edu.ucdavis.fiehnlab.ms.carrot.core.db.mona

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Target}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.SpanSugar._
import org.scalatest.{ShouldMatchers, WordSpec}
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
class MonaLibraryAccessTest extends WordSpec with ShouldMatchers with LazyLogging with Eventually {
  val testTarget = new Target {
    /**
      * a name for this spectra
      */
    override val name: Option[String] = None
    /**
      * retention time in seconds of this target
      */
    override val retentionIndex: Double = 100
    /**
      * the unique inchi key for this spectra
      */
    override val inchiKey: Option[String] = Option("UDOOPSJCRMKSGL-ZHACJKMWSA-N")
    /**
      * the mono isotopic mass of this spectra
      */
    override val precursorMass: Option[Double] = Option(224.0837)
    /**
      * is this a confirmed target
      */
    override val confirmed: Boolean = false
    /**
      * is this target required for a successful retention index correction
      */
    override val requiredForCorrection: Boolean = true
    /**
      * is this a retention index correction standard
      */
    override val isRetentionIndexStandard: Boolean = true
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
    override val name: Option[String] = None
    /**
      * retention time in seconds of this target
      */
    override val retentionIndex: Double = 100.5
    /**
      * the unique inchi key for this spectra
      */
    override val inchiKey: Option[String] = Option("UDOOPSJCRMKSGL-ZHACJKMWSA-N")
    /**
      * the mono isotopic mass of this spectra
      */
    override val precursorMass: Option[Double] = Option(224.0831)
    /**
      * is this a confirmed target
      */
    override val confirmed: Boolean = false
    /**
      * is this target required for a successful retention index correction
      */
    override val requiredForCorrection: Boolean = true
    /**
      * is this a retention index correction standard
      */
    override val isRetentionIndexStandard: Boolean = true
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

    "generateTarget" in {

      val result = library.generateSpectrum(testTarget, AcquisitionMethod(None), None)

      result.isDefined shouldBe true

      result.get.spectrum shouldBe "100.021000:123.12300 224.083700:1231.02100"

      result.get.compound.length shouldBe 1

      result.get.compound.head.names.length shouldBe 1

      result.get.compound.head.names.head.name shouldBe s"unknown_100.0000_224.0837"

      result.get.compound.head.inchiKey shouldBe "UDOOPSJCRMKSGL-ZHACJKMWSA-N"

    }

    "delete existing data" in {
      library.libraries.foreach { x =>
        logger.info(s"deleting library: ${x}")
        library.load(x).foreach { y =>
          logger.info(s"deleting spectra: ${y} in ${x}")
          library.delete(y, x)
        }
      }
      eventually(timeout(5 seconds)) {
        client.list().size shouldBe 0
        Thread.sleep(250)
      }
    }


    "there should be 0 acquisition methods defined now" in {
      eventually(timeout(5 seconds)) {
        library.libraries.size shouldBe 0
        Thread.sleep(250)
      }
    }

    "be possible to add and load targets" in {

      val acquisitionMethod: AcquisitionMethod = AcquisitionMethod(None)
      library.add(testTarget, acquisitionMethod, None)

      eventually(timeout(5 seconds)) {
        library.load(acquisitionMethod).size shouldBe 1
        Thread.sleep(250)
      }
      library.add(testTarget2, acquisitionMethod, None)

      eventually(timeout(5 seconds)) {
        library.load(acquisitionMethod).size shouldBe 2
        Thread.sleep(250)
      }

    }

    "be possible to add and load targets from a different library" in {

      val acquisitionMethod: AcquisitionMethod = AcquisitionMethod(Option(ChromatographicMethod("test", None, None, None)))
      library.add(testTarget, acquisitionMethod, None)
      eventually(timeout(5 seconds)) {
        library.load(acquisitionMethod).size shouldBe 1
        Thread.sleep(250)
      }
      library.add(testTarget2, acquisitionMethod, None)
      eventually(timeout(5 seconds)) {
        library.load(acquisitionMethod).size shouldBe 2
        Thread.sleep(250)
      }

      eventually(timeout(5 seconds)) {
        library.load(AcquisitionMethod(None)).size shouldBe 2
        Thread.sleep(250)
      }
      eventually(timeout(5 seconds)) {

        val count: Int = library.libraries.map { x =>
          library.load(x).map { y =>
            1
          }.sum
        }.sum

        count shouldBe 4

        Thread.sleep(250)
      }
    }

    "there should be 2 acquisition methods defined now" in {
      eventually(timeout(90 seconds)) {
        library.libraries.size shouldBe 2
        Thread.sleep(250)
      }
    }

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[JWTAuthenticationConfig]))
class MonaLibraryAccessTestConfiguration {

}