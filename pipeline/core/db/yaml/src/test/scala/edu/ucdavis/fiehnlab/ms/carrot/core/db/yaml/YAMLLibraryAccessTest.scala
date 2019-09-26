package edu.ucdavis.fiehnlab.ms.carrot.core.db.yaml

import java.util

import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, LibraryAccess, MergeLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.collection.JavaConverters._

@SpringBootTest
@ActiveProfiles(Array("test", "carrot.targets.yaml.correction", "carrot.targets.yaml.annotation"))
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
  }
}

@SpringBootTest
@ActiveProfiles(Array("test", "carrot.targets.yaml.correction", "carrot.targets.yaml.annotation"))
class YAMLLibraryConfigurationAnnotationTest extends WordSpec with Matchers {

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


@SpringBootTest
@ActiveProfiles(Array("test", "carrot.targets.yaml.correction", "carrot.targets.yaml.annotation"))
class YAMLLibraryConfigurationCorrectionTest extends WordSpec with Matchers {

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


@SpringBootTest
@ActiveProfiles(Array("test", "carrot.targets.yaml.correction", "carrot.targets.yaml.annotation"))
class YAMLLibraryConfigurationHILICTest extends WordSpec with Matchers {

  @Autowired
  val libraryAccess: MergeLibraryAccess = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "YAMLLibraryAccess with mergedLibraryAccess" should {
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

@SpringBootTest
@ActiveProfiles(Array("test", "carrot.targets.yaml.correction", "carrot.targets.yaml.annotation"))
class YAMLLibraryConfigurationMultipleTest extends WordSpec with Matchers {

}


@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class YAMLLibraryAccessTestConfiguration {
  @Autowired
  val corrLib: YAMLCorrectionLibraryAccess = null

  @Autowired
  val annLib: YAMLAnnotationLibraryAccess = null

  @Bean
  def libraryAccess: MergeLibraryAccess = new MergeLibraryAccess(
    new DelegateLibraryAccess[CorrectionTarget](new util.ArrayList(Seq(corrLib).asJavaCollection)),
    new DelegateLibraryAccess[AnnotationTarget](new util.ArrayList(Seq(annLib).asJavaCollection)))

}
