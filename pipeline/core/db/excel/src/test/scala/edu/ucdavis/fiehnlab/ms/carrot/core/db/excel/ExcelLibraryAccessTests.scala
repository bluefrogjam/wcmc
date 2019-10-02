package edu.ucdavis.fiehnlab.ms.carrot.core.db.excel

import java.util

import edu.ucdavis.fiehnlab.loader.ResourceLoader
import edu.ucdavis.fiehnlab.loader.impl.RecursiveDirectoryResourceLoader
import edu.ucdavis.fiehnlab.ms.carrot.core.api.io.{DelegateLibraryAccess, MergeLibraryAccess}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.{AnnotationTarget, CorrectionTarget, NegativeMode, PositiveMode}
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.{AcquisitionMethod, ChromatographicMethod}
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Profile}
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.collection.JavaConverters._

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("test", "carrot.targets.excel.correction", "carrot.targets.excel.annotation", "positive"))
class ExcelLibraryAccessPositiveModeTests extends WordSpec with Matchers {

  @Autowired
  val library: MergeLibraryAccess = null

  @Autowired
  val corrLib: ExcelCorrectionLibraryAccess = null

  @Autowired
  val annLib: ExcelAnnotationLibraryAccess = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ExcelLibraryAccess in positive mode" should {
    val posMethod = AcquisitionMethod(ChromatographicMethod("test-pos", Some("test"), Some("test"), Some(PositiveMode())))
    val negMethod = AcquisitionMethod(ChromatographicMethod("test-neg", Some("test"), Some("test"), Some(NegativeMode())))

    "have correction targets in correction delegate" in {
      corrLib.load(posMethod) should have size 2
    }

    "have annotation targets in annotation delegate" in {
      annLib.load(posMethod) should have size 2
    }

    "load an excel file" in {
      val data = library.load(posMethod)
      data should not be empty
      data should have size 4
    }

    "have one library" in {
      library.libraries should have size 1
      library.libraries.head should equal(posMethod)
    }

    "return no targets for other libraries" in {
      library.load(negMethod) should have size 0
    }
  }
}

@RunWith(classOf[SpringRunner])
@SpringBootTest
@ActiveProfiles(Array("test", "carrot.targets.excel.correction", "carrot.targets.excel.annotation", "negative"))
class ExcelLibraryAccessNegativeModeTests extends WordSpec with Matchers {

  @Autowired
  val library: MergeLibraryAccess = null

  @Autowired
  val corrLib: ExcelCorrectionLibraryAccess = null

  @Autowired
  val annLib: ExcelAnnotationLibraryAccess = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ExcelLibraryAccess in negative mode" should {
    val posMethod = AcquisitionMethod(ChromatographicMethod("test-pos", Some("test"), Some("test"), Some(PositiveMode())))
    val negMethod = AcquisitionMethod(ChromatographicMethod("test-neg", Some("test"), Some("test"), Some(NegativeMode())))

    "have correction targets in correction delegate" in {
      corrLib.load(negMethod) should have size 2
    }

    "have annotation targets in annotation delegate" in {
      annLib.load(negMethod) should have size 2
    }

    "load an excel file" in {
      val data = library.load(negMethod)
      data should not be empty
    }

    "have library data" in {
      library.libraries should not be empty
      library.libraries.head should equal(negMethod)
    }

    "return no targets for other libraries" in {
      library.load(posMethod) should have size 0
    }
  }
}

@SpringBootApplication(exclude = Array(classOf[DataSourceAutoConfiguration]))
class ExcelLibraryAccessConfiguration {
  @Autowired
  val corrLib: ExcelCorrectionLibraryAccess = null

  @Autowired
  val annLib: ExcelAnnotationLibraryAccess = null

  @Bean
  @Profile(Array("positive"))
  def positiveMode: ExcelLibraryConfigurationProperties = new ExcelLibraryConfigurationProperties()

  @Bean
  @Profile(Array("negative"))
  def negativeMode: ExcelLibraryConfigurationProperties = new ExcelLibraryConfigurationProperties()

  @Bean
  def libraryAccess: MergeLibraryAccess = new MergeLibraryAccess(
    new DelegateLibraryAccess[CorrectionTarget](new util.ArrayList(Seq(corrLib).asJavaCollection)),
    new DelegateLibraryAccess[AnnotationTarget](new util.ArrayList(Seq(annLib).asJavaCollection)))

  @Bean
  def loader: ResourceLoader = new RecursiveDirectoryResourceLoader(new java.io.File("./"))
}
