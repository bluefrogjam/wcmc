package edu.ucdavis.fiehnlab.ms.carrot.core.db.yaml

import edu.ucdavis.fiehnlab.loader.{DelegatingResourceLoader, ResourceLoader}
import edu.ucdavis.fiehnlab.ms.carrot.cloud.aws.AWSConfiguration
import edu.ucdavis.fiehnlab.ms.carrot.cloud.bucket.{BucketLoader, BucketStorageConfiguration}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{LibraryAccess, MergeLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@SpringBootTest(classes = Array(
  classOf[YAMLLibraryAccessTestConfiguration],
  classOf[BucketStorageConfiguration],
  classOf[AWSConfiguration]
))
@ActiveProfiles(Array("test",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation",
  "carrot.resource.loader.bucket"
))
class AwsYamlLibraryAccessTest extends WordSpec with Matchers {

  @Autowired
  val loader: ResourceLoader = null

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
  }
}

@SpringBootTest
@ActiveProfiles(Array("test",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation",
  "carrot.resource.loader.bucket"))
class AwsYamlLibraryAccessAnnotationTest extends WordSpec with Matchers {

  @Autowired
  val libraryAccess: LibraryAccess[AnnotationTarget] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "AWSYamlLibraryAccess for annotation targets" should {

    "load teddy library" in {

      val data = libraryAccess.load(AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Some(PositiveMode()))))

      data should have size 939
    }
  }
}


@SpringBootTest
@ActiveProfiles(Array("test",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation",
  "carrot.resource.loader.bucket"
))
class AwsYamlLibraryAccessCorrectionTest extends WordSpec with Matchers {
  @Autowired
  val loader: ResourceLoader = null

  @Autowired
  val libraryAccess: LibraryAccess[CorrectionTarget] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "AWSYamlLibraryConfiguration for correction targets" should {

    "have a bucket based resource loader" in {
      loader.asInstanceOf[DelegatingResourceLoader].loaders should have size 1

    }

    "load teddy correction targets" in {
      val data = libraryAccess.load(AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Some(PositiveMode()))))
      data should have size 25
    }
  }
}


@SpringBootTest
@ActiveProfiles(Array("test",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation",
  "carrot.resource.loader.bucket"
))
class AwsYamlLibraryAccessHILICTest extends WordSpec with Matchers {

  @Autowired
  val libraryAccess: MergeLibraryAccess = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "YAMLLibraryAccess with merged libraries" should {
    "be able to load HILIC QExactive library" in {
      libraryAccess.libraries.filter(_.chromatographicMethod.name == "hilic_qehf") should have size 2
      libraryAccess.libraries.filter(_.chromatographicMethod.name == "hilic_qehf").foreach(l => println(l.chromatographicMethod))
    }

    "be able to load HILIC QTof library" in {
      libraryAccess.libraries.filter(_.chromatographicMethod.name == "hilic_qtof") should have size 2
      libraryAccess.libraries.filter(_.chromatographicMethod.name == "hilic_qtof").foreach(l => println(l.chromatographicMethod))
    }
  }
}
