package edu.ucdavis.fiehnlab.ms.carrot.core.db.yaml

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.cloud.bucket.BucketLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{CorrectionTarget, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.apache.logging.log4j.scala.Logging
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@SpringBootTest(classes = Array(classOf[YAMLLibraryAccessTestConfiguration]))
@ActiveProfiles(Array("test",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation",
  "carrot.resource.loader.bucket.data",
  "carrot.resource.store.bucket.data"
))
class AwsYamlLibraryAccessTest extends WordSpec with Matchers with Logging {

  @Autowired
  @Qualifier("dataLoader")
  val loader: ResourceLoader = null

  @Autowired
  val libraryAccess: LibraryAccess[CorrectionTarget] = null

  @Autowired
  val library: YAMLLibraryAccess = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "YAMLLibraryAccess" should {

    "have a BuckerLoader " in {
      loader shouldBe a[BucketLoader]
    }

    "fail trying to load not existing method" in {
      an[Exception] should be thrownBy library.load(AcquisitionMethod(ChromatographicMethod("", Some(""), Some(""), Some(PositiveMode()))))
    }

    "load existing method" in {
      noException should be thrownBy library.load(AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Some(PositiveMode()))))
    }

    "must have libraries" in {
      library.libraries should not be empty
    }

    "load teddy correction targets" in {
      val data = libraryAccess.load(AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Some(PositiveMode()))))
      data should have size 25
    }

    "be able to load HILIC QExactive library" in {
      libraryAccess.libraries.filter(_.chromatographicMethod.name == "hilic_qehf") should have size 2
      libraryAccess.libraries.filter(_.chromatographicMethod.name == "hilic_qehf").foreach(l => logger.info(l.chromatographicMethod.toString))
    }

    "be able to load HILIC QTof library" in {
      libraryAccess.libraries.filter(_.chromatographicMethod.name == "hilic_qtof") should have size 2
      libraryAccess.libraries.filter(_.chromatographicMethod.name == "hilic_qtof").foreach(l => logger.info(l.chromatographicMethod.toString))
    }
  }
}
