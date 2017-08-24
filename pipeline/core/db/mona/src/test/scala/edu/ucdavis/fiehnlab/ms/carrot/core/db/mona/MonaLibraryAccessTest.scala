package edu.ucdavis.fiehnlab.ms.carrot.core.db.mona

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.AcquisitionMethod
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.SpectrumProperties
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{Ion, Target}
import org.scalatest.{ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 8/14/17.
  */

@SpringBootTest
@ActiveProfiles(Array("backend-mona"))
class MonaLibraryAccessTest extends WordSpec with ShouldMatchers with LazyLogging{
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



  @Value("${mona.rest.server.user}")
  val username:String = null

  @Value("${mona.rest.server.password}")
  val password:String = null

  @Autowired
  val library: MonaLibraryAccess = null

  @Autowired
  val userRepo: UserRepository = null

  @Autowired
  val monaSpectrumRestClient:MonaSpectrumRestClient = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MonaLibraryAccessTest" should {

    "create user to authenticate" in {
      val result = userRepo.save(User(username,password, Array(Role("ADMIN")).toList.asJava))
    }

    "generateTarget" in {

      val result = library.generateSpectrum(testTarget,new AcquisitionMethod(None,None))

      result.isDefined shouldBe true

      result.get.spectrum shouldBe "100.021000:123.12300 224.083700:1231.02100"

      result.get.compound.length shouldBe 1

      result.get.compound.head.names.length shouldBe 1

      result.get.compound.head.names.head.name shouldBe s"unknown_${100.0}_${224.0837}"

      result.get.compound.head.inchiKey shouldBe "UDOOPSJCRMKSGL-ZHACJKMWSA-N"

    }

    "be possible to add and load targets" in {
      monaSpectrumRestClient.login(username,password)
      monaSpectrumRestClient.list().foreach(s => monaSpectrumRestClient.delete(s.id))

      val acquisitionMethod:AcquisitionMethod = new AcquisitionMethod(None,None)
      library.add(testTarget,acquisitionMethod)
        library.load(acquisitionMethod).size shouldBe 1

        library.add(testTarget2,acquisitionMethod)
        library.load(acquisitionMethod).size shouldBe 2

    }

  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
@Import(Array(classOf[JWTAuthenticationConfig]))
class MonaLibraryAccessTestConfiguration {

}