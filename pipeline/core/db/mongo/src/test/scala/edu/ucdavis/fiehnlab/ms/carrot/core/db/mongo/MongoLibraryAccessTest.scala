package edu.ucdavis.fiehnlab.ms.carrot.core.db.mongo

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Target
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach, ShouldMatchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlgemuth on 8/9/17.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[MongoLibraryAccessConfig]))
class MongoLibraryAccessTest extends WordSpec with BeforeAndAfterEach with ShouldMatchers {

  @Autowired
  val library: MongoLibraryAccess = null

  @Autowired
  val libraryRepository: ILibraryRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "MongoLibraryAccessTest" should {

    "be able to add a target" in {
      library.load.size should be(0)

      library.add(new Target {
        /**
          * the unique inchi key for this spectra
          */
        override val inchiKey: Option[String] = None
        /**
          * retention time in seconds of this target
          */
        override val retentionTimeInSeconds: Double = 123
        /**
          * a name for this spectra
          */
        override val name: Option[String] = Some("Test")
        /**
          * the mono isotopic mass of this spectra
          */
        override val monoIsotopicMass: Option[Double] = Some(100)
        /**
          * is this a confirmed target
          */
        override val confirmedTarget: Boolean = false
        /**
          * is this target required for a successful retention index correction
          */
        override val requiredForCorrection: Boolean = false
        /**
          * is this a retention index correction standard
          */
        override val isRetentionIndexStandard: Boolean = false
      })

      library.load.size should be(1)


      library.add(new Target {
        /**
          * the unique inchi key for this spectra
          */
        override val inchiKey: Option[String] = None
        /**
          * retention time in seconds of this target
          */
        override val retentionTimeInSeconds: Double = 123.5
        /**
          * a name for this spectra
          */
        override val name: Option[String] = Some("Test-2")
        /**
          * the mono isotopic mass of this spectra
          */
        override val monoIsotopicMass: Option[Double] = Some(100.2)
        /**
          * is this a confirmed target
          */
        override val confirmedTarget: Boolean = false
        /**
          * is this target required for a successful retention index correction
          */
        override val requiredForCorrection: Boolean = false
        /**
          * is this a retention index correction standard
          */
        override val isRetentionIndexStandard: Boolean = false
      })

      library.load.size should be(2)


      library.add(new Target {
        /**
          * the unique inchi key for this spectra
          */
        override val inchiKey: Option[String] = None
        /**
          * retention time in seconds of this target
          */
        override val retentionTimeInSeconds: Double = 124.5
        /**
          * a name for this spectra
          */
        override val name: Option[String] = Some("Test-3")
        /**
          * the mono isotopic mass of this spectra
          */
        override val monoIsotopicMass: Option[Double] = None
        /**
          * is this a confirmed target
          */
        override val confirmedTarget: Boolean = false
        /**
          * is this target required for a successful retention index correction
          */
        override val requiredForCorrection: Boolean = false
        /**
          * is this a retention index correction standard
          */
        override val isRetentionIndexStandard: Boolean = false
      })

      library.load.size should be(3)

      library.load.toList.maxBy(_.retentionTimeInSeconds).monoIsotopicMass should be(None)

    }


  }

  override protected def beforeEach(): Unit = {
    libraryRepository.deleteAll()
  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class MongoLibraryAccessConfig {

  @Bean
  def library(libraryRepository: ILibraryRepository): MongoLibraryAccess = new MongoLibraryAccess(libraryRepository, "test")
}