package edu.ucdavis.fiehnlab.ms.carrot.core.db.yaml

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.LibraryAccess
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

@SpringBootTest
@ActiveProfiles(Array("test", "carrot.targets.yaml.annotation"))
class YAMLLibraryAccessTest extends WordSpec {

  @Autowired
  val library: YAMLLibraryAccess = null


  new TestContextManager(this.getClass).prepareTestInstance(this)

  "YAMLLibraryAccessTest" should {


    "load method does not exist must fail" in {
      try {

        library.load(AcquisitionMethod(ChromatographicMethod("", Some(""), Some(""), Some(PositiveMode()))))
        fail()
      }
      catch {
        case e: Exception =>
        //pass

      }
    }

    "load method does not exist must pass" in {
      try {

        library.load(AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Some(PositiveMode()))))
      }
      catch {
        case e: Exception =>
          fail()
      }
    }

    "must have libraries" in {
      val lib = library.libraries

      assert(lib.size > 0)
    }
  }
}

@SpringBootTest
@ActiveProfiles(Array("test", "carrot.targets.yaml.annotation"))
class YAMLLibraryConfigurationAnnotationTest extends WordSpec {

  @Autowired
  val libraryAccess: LibraryAccess[AnnotationTarget] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "we should be able to load annotation targets" should {

    "load teddy library" in {

      val data = libraryAccess.load(AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Some(PositiveMode()))))

      assert(data.size > 0)
    }
  }
}


@SpringBootTest
@ActiveProfiles(Array("test", "carrot.targets.yaml.correction"))
class YAMLLibraryConfigurationCorrectionTest extends WordSpec {

  @Autowired
  val libraryAccess: LibraryAccess[CorrectionTarget] = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "we should be able to load correction targets" should {

    "load teddy library" in {

      val data = libraryAccess.load(AcquisitionMethod(ChromatographicMethod("teddy", Some("6530"), Some("test"), Some(PositiveMode()))))
    }
  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class YAMLLibraryAccessTestConfiguration {

}
