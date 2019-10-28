package edu.ucdavis.fiehnlab.ms.carrot.core.db.yaml

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@SpringBootTest(classes = Array(classOf[YAMLLibraryAccessTestConfiguration]))
@ActiveProfiles(Array("test",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation"))
class YAMLLibraryAccessTest extends WordSpec with Matchers {

  @Autowired
  val library: YAMLLibraryAccess = null


  new TestContextManager(this.getClass).prepareTestInstance(this)

  "YAMLLibraryAccessTest" should {

    "fail trying to load not existing method" in {
      an[Exception] should be thrownBy library.load(AcquisitionMethod(ChromatographicMethod("", Some(""), Some(""), Some(PositiveMode()))))
    }

    "load existing method" in {
      noException should be thrownBy library.load(AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Some(PositiveMode()))))
    }

    "must have libraries" in {
      library.libraries should not be empty
    }

    "be able to load HILIC QExactive library" in {
      library.libraries.filter(_.chromatographicMethod.name == "hilic_qehf") should have size 2
      library.libraries.filter(_.chromatographicMethod.name == "hilic_qehf").foreach(l => println(l.chromatographicMethod))
    }

    "be able to load HILIC QTof library" in {
      library.libraries.filter(_.chromatographicMethod.name == "hilic_qtof") should have size 2
      library.libraries.filter(_.chromatographicMethod.name == "hilic_qtof").foreach(l => println(l.chromatographicMethod))
    }
  }
}

@SpringBootTest(classes = Array(classOf[YAMLLibraryAccessTestConfiguration]))
@ActiveProfiles(Array("test",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation"))
class YAMLLibraryAccessAnnotationTest extends WordSpec with Matchers {

  @Autowired
  val libraryAccess: LibraryAccess[AnnotationTarget] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "we should be able to load annotation targets" should {

    "load teddy library" in {

      val data = libraryAccess.load(AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Some(PositiveMode()))))

      data should have size 939
    }
  }
}

@SpringBootTest(classes = Array(classOf[YAMLLibraryAccessTestConfiguration]))
@ActiveProfiles(Array("test",
  "carrot.targets.yaml.correction",
  "carrot.targets.yaml.annotation"))
class YAMLLibraryAccessCorrectionTest extends WordSpec with Matchers {

  @Autowired
  val libraryAccess: LibraryAccess[CorrectionTarget] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "we should be able to load correction targets" should {

    "load teddy library" in {

      val data = libraryAccess.load(AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Some(PositiveMode()))))

      data should have size 25
    }
  }
}
